package uk.gov.bis.lite.permissions.service;

import static java.time.temporal.ChronoUnit.MINUTES;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.FailEvent;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Singleton
public class ProcessSubmissionServiceImpl implements ProcessSubmissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessSubmissionServiceImpl.class);

  private static final Set<OgelSubmission.FailReason> STATUS_COMPLETE_FAIL_REASONS = EnumSet.of(
      OgelSubmission.FailReason.BLACKLISTED,
      OgelSubmission.FailReason.PERMISSION_DENIED,
      OgelSubmission.FailReason.SITE_ALREADY_REGISTERED);

  /**
   * Origin of any FailEvent: CUSTOMER, SITE, USER_ROLE, OGEL_CREATE, CALLBACK, UNKNOWN
   */
  public enum Origin {
    CUSTOMER, SITE, USER_ROLE, OGEL_CREATE, CALLBACK, UNKNOWN
  }

  private final OgelSubmissionDao submissionDao;
  private final CustomerService customerService;
  private final OgelService ogelService;
  private final CallbackService callbackService;

  private final int maxMinutesRetryAfterFail;
  private final int maxCallbackFailCount;

  @Inject
  public ProcessSubmissionServiceImpl(OgelSubmissionDao submissionDao, CustomerService customerService,
                                      OgelService ogelService, CallbackService callbackService,
                                      @Named("maxMinutesRetryAfterFail") int maxMinutesRetryAfterFail,
                                      @Named("maxCallbackFailCount") int maxCallbackFailCount) {
    this.submissionDao = submissionDao;
    this.customerService = customerService;
    this.ogelService = ogelService;
    this.callbackService = callbackService;
    this.maxMinutesRetryAfterFail = maxMinutesRetryAfterFail;
    this.maxCallbackFailCount = maxCallbackFailCount;
  }

  /**
   * Process OgelSubmission through all stages - set Mode to SCHEDULED if process cannot be completed
   */
  @Override
  public void processImmediate(long submissionId) {
    LOGGER.info("IMMEDIATE SubID[{}]", submissionId);

    OgelSubmission sub = submissionDao.findBySubmissionId(submissionId);
    try {

      // Process this OgelSubmission immediately
      doProcessOgelSubmission(sub);

      // If no FailEvent we attempt callback
      boolean updateToScheduled = sub.hasFailEvent();
      if (!updateToScheduled && !callbackService.completeCallback(sub)) {
        // We have callback failure so we need to ensure OgelSubmission mode is set to SCHEDULED
        updateToScheduled = true;
      }

      // Change mode of OgelSubmission to SCHEDULED if we have had a previous failure
      if (updateToScheduled) {
        LOGGER.info("Setting submission MODE to SCHEDULED: SubID[{}]", submissionId);
        updateForProcessFailure(sub);
        sub.setScheduledMode();
      }

      // Update state of OgelSubmission
      submissionDao.update(sub);

    } catch (Exception exception) {
      errorThrown(sub, exception, "ProcessSubmissionServiceImpl.processImmediate");
    }
  }

  /**
   * Processes SCHEDULED OgelSubmissions through stages
   * Processes SCHEDULED OgelSubmissions callbacks
   */
  public void processOgelSubmissions() {
    processScheduled();
    processCallbacks();
  }

  /**
   * Attempts to process OgelSubmission through each stage
   */
  @VisibleForTesting
  public void doProcessOgelSubmission(OgelSubmission sub) {

    OgelSubmission.Stage stage = sub.getStage();
    if (stage == OgelSubmission.Stage.CREATED) {
      stage = progressStage(sub);
      submissionDao.update(sub);
    }

    if (stage == OgelSubmission.Stage.CUSTOMER) {
      if (processForCustomer(sub)) {
        stage = progressStage(sub);
      } else {
        updateForProcessFailure(sub);
      }
      submissionDao.update(sub);
    }

    if (stage == OgelSubmission.Stage.SITE) {
      if (processForSite(sub)) {
        stage = progressStage(sub);
      } else {
        updateForProcessFailure(sub);
      }
      submissionDao.update(sub);
    }

    if (stage == OgelSubmission.Stage.USER_ROLE) {
      if (processForUserRole(sub)) {
        stage = progressStage(sub);
      } else {
        updateForProcessFailure(sub);
      }
      submissionDao.update(sub);
    }

    if (stage == OgelSubmission.Stage.OGEL) {
      if (processForOgel(sub)) {
        sub.updateStatusToComplete();
      } else {
        updateForProcessFailure(sub);
      }
      submissionDao.update(sub);
    }
  }

  /**
   * Updates and returns OgelSubmission STAGE
   */
  @VisibleForTesting
  public OgelSubmission.Stage progressStage(OgelSubmission sub) {
    if (hasCompletedCurrentStage(sub)) {
      OgelSubmission.Stage nextStage = getNextStage(sub.getStage());
      if (nextStage != null) {
        sub.setStage(nextStage);
        if (hasCompletedStage(sub, nextStage)) {
          return progressStage(sub);
        }
      }
      return sub.getStage();
    } else {
      return sub.getStage();
    }
  }

  /**
   * Find ACTIVE SCHEDULED OgelSubmissions and attempt to process each through all stages.
   */
  private void processScheduled() {
    List<OgelSubmission> subs = submissionDao.getScheduledActive();
    LOGGER.debug("SCHEDULED ACTIVE Size[{}]", subs.size());
    for (OgelSubmission sub : subs) {
      try {
        doProcessOgelSubmission(sub);
      } catch (Exception exception) {
        errorThrown(sub, exception, "ProcessSubmissionServiceImpl.processScheduled");
      }
    }
  }

  /**
   * Find COMPLETE scheduled OgelSubmissions to callback and attempt callback
   */
  private void processCallbacks() {
    List<OgelSubmission> subs = submissionDao.getScheduledCompleteToCallback();
    LOGGER.debug("SCHEDULED CALLBACK Size[{}]", subs.size());
    for (OgelSubmission sub : subs) {
      try {
        if (!callbackService.completeCallback(sub)) {
          updateForCallbackFailure(sub);
        }
        submissionDao.update(sub);
      } catch (Exception exception) {
        errorThrown(sub, exception, "ProcessSubmissionServiceImpl.processScheduled");
      }
    }
  }

  /**
   * Updates OgelSubmission for Callback failures, checks fail count to set to TERMINATED if necessary
   * Removes FailEvent once OgelSubmission updated
   */
  @VisibleForTesting
  void updateForCallbackFailure(OgelSubmission sub) {
    if (sub.hasFailEvent()) {
      FailEvent event = sub.getFailEvent();
      OgelSubmission.FailReason failReason = event.getFailReason();
      Origin origin = event.getOrigin();
      String message = event.getMessage();

      String failMessage = createFailMessage(failReason, origin, message);
      LOGGER.error("{} SubID[{}]", failMessage, sub.getId());

      int currentCallbackFailCount = sub.getCallBackFailCount();

      // Check for repeating error
      if (currentCallbackFailCount > maxCallbackFailCount) {
        LOGGER.info("Repeating Callback Error - setting status to TERMINATED SubID[{}]", sub.getId());
        sub.updateStatusToTerminated();
      } else {
        sub.setCallBackFailCount(currentCallbackFailCount + 1);
      }

      // Remove FailEvent
      sub.clearFailEvent();
    }
  }

  /**
   * Updates OgelSubmission with any FailEvent data
   * Removes FailEvent once OgelSubmission updated
   */
  @VisibleForTesting
  void updateForProcessFailure(OgelSubmission sub) {
    if (sub.hasFailEvent()) {
      FailEvent event = sub.getFailEvent();
      OgelSubmission.FailReason failReason = event.getFailReason();
      Origin origin = event.getOrigin();
      String message = event.getMessage();

      String failMessage = createFailMessage(failReason, origin, message);
      LOGGER.error("{} SubID[{}]", failMessage, sub.getId());

      if (!sub.hasFail()) {
        sub.setFirstFail(LocalDateTime.now()); // Set first fail
      } else {
        // Check for repeating error
        if (sub.getFirstFail().isBefore(LocalDateTime.now().minus(maxMinutesRetryAfterFail, MINUTES))) {
          LOGGER.info("Repeating Error - setting status to COMPLETE SubID[{}]", sub.getId());
          sub.updateStatusToComplete();
        }
      }

      sub.setFailReason(failReason);
      sub.setLastFailMessage(failMessage);
      sub.setLastFail(LocalDateTime.now());

      // Set status to complete with configured fail reason
      if (STATUS_COMPLETE_FAIL_REASONS.contains(failReason)) {
        LOGGER.info("Found setStatusComplete FailReason - setting status to COMPLETE SubID[{}]", sub.getId());
        sub.updateStatusToComplete();
      } else {
        LOGGER.info("FailReason {} - status remains {} SubID[{}]", failReason, sub.getStatus(), sub.getId());
      }

      // Remove FailEvent
      sub.clearFailEvent();
    }
  }

  private String createFailMessage(OgelSubmission.FailReason failReason, Origin origin, String message) {
    String failMessage = "FailReason[" + failReason.name() + "] Origin[" + origin.name() + "]";
    if (!StringUtils.isBlank(message)) {
      failMessage = failMessage + " - " + message;
    }
    return failMessage;
  }

  private boolean processForCustomer(OgelSubmission sub) {
    Optional<String> sarRef = customerService.getOrCreateCustomer(sub);
    if (sarRef.isPresent()) {
      sub.setCustomerRef(sarRef.get());
      LOGGER.info("SubID[{}] OgelSubmission CUSTOMER created SarRef[{}]", sub.getId(), sarRef.get());
      return true;
    }
    return false;
  }

  private boolean processForSite(OgelSubmission sub) {
    Optional<String> siteRef = customerService.createSite(sub);
    if (siteRef.isPresent()) {
      sub.setSiteRef(siteRef.get());
      LOGGER.info("SubID[{}] OgelSubmission SITE created SiteRef[{}]", sub.getId(), siteRef.get());
      return true;
    }
    return false;
  }

  private boolean processForUserRole(OgelSubmission sub) {
    boolean updated = customerService.updateUserRole(sub);
    if (updated) {
      sub.setRoleUpdated(true);
      LOGGER.info("SubID[{}] OgelSubmission USER_ROLE updated UserID[{}] OgelType[{}]", sub.getId(), sub.getUserId(), sub.getOgelType());
      return true;
    }
    return false;
  }

  private boolean processForOgel(OgelSubmission sub) {
    Optional<String> spireRef = ogelService.createOgel(sub);
    if (spireRef.isPresent()) {
      sub.setSpireRef(spireRef.get());
      LOGGER.info("SubID[{}] OgelSubmission OGEL created SpireRef[{}]", sub.getId(), spireRef.get());
      return true;
    }
    return false;
  }

  private OgelSubmission.Stage getNextStage(OgelSubmission.Stage stage) {
    if (stage == OgelSubmission.Stage.CREATED) {
      return OgelSubmission.Stage.CUSTOMER;
    } else if (stage == OgelSubmission.Stage.CUSTOMER) {
      return OgelSubmission.Stage.SITE;
    } else if (stage == OgelSubmission.Stage.SITE) {
      return OgelSubmission.Stage.USER_ROLE;
    } else if (stage == OgelSubmission.Stage.USER_ROLE) {
      return OgelSubmission.Stage.OGEL;
    } else {
      return null;
    }
  }

  private boolean hasCompletedStage(OgelSubmission sub, OgelSubmission.Stage stage) {
    if (stage == OgelSubmission.Stage.CREATED) {
      return true;
    } else if (stage == OgelSubmission.Stage.CUSTOMER) {
      return StringUtils.isNotBlank(sub.getCustomerRef());
    } else if (stage == OgelSubmission.Stage.SITE) {
      return StringUtils.isNotBlank(sub.getSiteRef());
    } else if (stage == OgelSubmission.Stage.USER_ROLE) {
      return !sub.isRoleUpdate() || sub.isRoleUpdated();
    } else if (stage == OgelSubmission.Stage.OGEL) {
      return StringUtils.isNotBlank(sub.getSpireRef());
    } else {
      return false;
    }
  }

  private boolean hasCompletedCurrentStage(OgelSubmission sub) {
    return hasCompletedStage(sub, sub.getStage());
  }

  private void errorThrown(OgelSubmission sub, Exception exception, String info) {
    String stackTrace = Throwables.getStackTraceAsString(exception);
    sub.setFailEvent(new FailEvent(OgelSubmission.FailReason.UNCLASSIFIED, Origin.UNKNOWN, stackTrace));
    LOGGER.error("{} SubID[{}]", info, sub.getId(), exception);
  }
}

package uk.gov.bis.lite.permissions.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.util.Util;

import java.util.List;
import java.util.Optional;

@Singleton
public class ProcessOgelSubmissionServiceImpl implements ProcessOgelSubmissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessOgelSubmissionServiceImpl.class);

  private OgelSubmissionDao submissionDao;
  private CustomerService customerService;
  private OgelService ogelService;
  private CallbackService callbackService;
  private FailService failService;

  @Inject
  public ProcessOgelSubmissionServiceImpl(OgelSubmissionDao submissionDao, CustomerService customerService,
                                          OgelService ogelService, CallbackService callbackService, FailService failService) {
    this.submissionDao = submissionDao;
    this.customerService = customerService;
    this.ogelService = ogelService;
    this.callbackService = callbackService;
    this.failService = failService;
  }

  /**
   * Process OgelSubmission through all stages - set Mode to SCHEDULED if process cannot be completed
   */
  public void processImmediate(int submissionId) {
    LOGGER.info("IMMEDIATE [" + submissionId + "]");

    OgelSubmission sub = submissionDao.findBySubmissionId(submissionId);
    try {

      // Process this OgelSubmission immediately
      doProcessOgelSubmission(sub);

      doCallbackOgelSubmission(sub);

      // Update MODE if necessary
      if (!sub.isCalledBack()) {
        LOGGER.info("Setting submission MODE to SCHEDULED: [" + submissionId + "]");
        sub.setScheduledMode();
        submissionDao.update(sub);
      }

    } catch (Throwable e) {
      errorThrown(sub, e, "ProcessOgelSubmissionServiceImpl.processImmediate");
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
    if(stage == OgelSubmission.Stage.CREATED) {
      stage = progressStage(sub);
      submissionDao.update(sub);
    }

    if(stage == OgelSubmission.Stage.CUSTOMER) {
      if(processForCustomer(sub)) {
        stage = progressStage(sub);
        submissionDao.update(sub);
      }
    }

    if(stage == OgelSubmission.Stage.SITE) {
      if(processForSite(sub)) {
        stage = progressStage(sub);
        submissionDao.update(sub);
      }
    }

    if(stage == OgelSubmission.Stage.USER_ROLE) {
      if(processForUserRole(sub)) {
        stage = progressStage(sub);
        submissionDao.update(sub);
      }
    }

    if(stage == OgelSubmission.Stage.OGEL) {
      if(processForOgel(sub)) {
        sub.updateStatusToComplete();
        submissionDao.update(sub);
      }
    }
  }

  /**
   * Updates and returns OgelSubmission STAGE
   */
  @VisibleForTesting
  public OgelSubmission.Stage progressStage(OgelSubmission sub) {
    if(hasCompletedCurrentStage(sub)) {
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
    LOGGER.info("SCHEDULED ACTIVE [" + subs.size() + "]");
    for (OgelSubmission sub : subs) {
      try {
        doProcessOgelSubmission(sub);
      } catch (Throwable e) {
        errorThrown(sub, e, "ProcessOgelSubmissionServiceImpl.processScheduled");
      }
    }
  }

  /**
   * Find COMPLETE scheduled OgelSubmissions to callback and attempt callback
   */
  private void processCallbacks() {
    List<OgelSubmission> subs = submissionDao.getScheduledCompleteToCallback();
    LOGGER.info("SCHEDULED COMPLETE CALLBACK [" + subs.size() + "]");
    for (OgelSubmission sub : subs) {
      try {
        doCallbackOgelSubmission(sub);
      } catch (Throwable e) {
        errorThrown(sub, e, "ProcessOgelSubmissionServiceImpl.processScheduled");
      }
    }
  }

  private boolean processForCustomer(OgelSubmission sub) {
    Optional<String> sarRef = customerService.getOrCreateCustomer(sub);
    if (sarRef.isPresent()) {
      sub.setCustomerRef(sarRef.get());
      LOGGER.info("[" + sub.getId() + "] OgelSubmission CUSTOMER created: " + sarRef.get());
      return true;
    }
    return false;
  }

  private boolean processForSite(OgelSubmission sub) {
    Optional<String> siteRef = customerService.createSite(sub);
    if (siteRef.isPresent()) {
      sub.setSiteRef(siteRef.get());
      LOGGER.info("[" + sub.getId() + "] OgelSubmission SITE created: " + siteRef.get());
      return true;
    }
    return false;
  }

  private boolean processForUserRole(OgelSubmission sub) {
    boolean updated = customerService.updateUserRole(sub);
    if (updated) {
      sub.setRoleUpdated(true);
      LOGGER.info("[" + sub.getId() + "] OgelSubmission USER_ROLE updated: " + sub.getUserId() + "/" + sub.getOgelType());
      return true;
    }
    return false;
  }

  private boolean processForOgel(OgelSubmission sub) {
    Optional<String> spireRef = ogelService.createOgel(sub);
    if (spireRef.isPresent()) {
      sub.setSpireRef(spireRef.get());
      LOGGER.info("[" + sub.getId() + "] OgelSubmission OGEL created: " + spireRef.get());
      return true;
    }
    return false;
  }

  private OgelSubmission.Stage getNextStage(OgelSubmission.Stage stage) {
    OgelSubmission.Stage nextStage = null;
    if (stage == OgelSubmission.Stage.CREATED) {
      nextStage = OgelSubmission.Stage.CUSTOMER;
    } else if (stage == OgelSubmission.Stage.CUSTOMER) {
      nextStage = OgelSubmission.Stage.SITE;
    } else if (stage == OgelSubmission.Stage.SITE) {
      nextStage = OgelSubmission.Stage.USER_ROLE;
    } else if (stage == OgelSubmission.Stage.USER_ROLE) {
      nextStage = OgelSubmission.Stage.OGEL;
    }
    return nextStage;
  }

  private boolean hasCompletedStage(OgelSubmission sub, OgelSubmission.Stage stage) {
    boolean completed = false;
    if (stage == OgelSubmission.Stage.CREATED) {
      completed = true;
    } else if (stage == OgelSubmission.Stage.CUSTOMER) {
      completed = !Util.isBlank(sub.getCustomerRef());
    } else if (stage == OgelSubmission.Stage.SITE) {
      completed = !Util.isBlank(sub.getSiteRef());
    } else if (stage == OgelSubmission.Stage.USER_ROLE) {
      completed = !sub.isRoleUpdate() || sub.isRoleUpdated();
    } else if (stage == OgelSubmission.Stage.OGEL) {
      completed = !Util.isBlank(sub.getSpireRef());
    }
    return completed;
  }

  private boolean hasCompletedCurrentStage(OgelSubmission sub) {
    return hasCompletedStage(sub, sub.getStage());
  }

  private void doCallbackOgelSubmission(OgelSubmission sub) {
    callbackService.completeCallback(sub);
  }

  private void errorThrown(OgelSubmission sub, Throwable e, String info) {
    String stackTrace = Throwables.getStackTraceAsString(e);
    failService.failWithMessage(sub, CallbackView.FailReason.UNCLASSIFIED, FailServiceImpl.Origin.UNKNOWN, stackTrace);
    LOGGER.error(info + ": " + e.getMessage(), e);
  }
}

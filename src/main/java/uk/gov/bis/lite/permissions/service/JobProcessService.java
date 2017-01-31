package uk.gov.bis.lite.permissions.service;

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

@Singleton
public class JobProcessService {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobProcessService.class);

  private OgelSubmissionDao submissionDao;
  private SubmissionService submissionService;
  private OgelService ogelService;
  private CallbackService callbackService;
  private FailService failService;

  @Inject
  public JobProcessService(OgelSubmissionDao submissionDao, SubmissionService submissionService,
                           OgelService ogelService, CallbackService callbackService, FailService failService) {
    this.submissionDao = submissionDao;
    this.submissionService = submissionService;
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
      errorThrown(sub, e, "JobProcessService.processImmediate");
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
   * Find ACTIVE SCHEDULED OgelSubmissions and attempt to process each through all stages.
   */
  private void processScheduled() {
    List<OgelSubmission> subs = submissionDao.getScheduledActive();
    LOGGER.info("SCHEDULED ACTIVE [" + subs.size() + "]");
    for (OgelSubmission sub : subs) {
      try {
        doProcessOgelSubmission(sub);
      } catch (Throwable e) {
        errorThrown(sub, e, "JobProcessService.processScheduled");
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
        errorThrown(sub, e, "JobProcessService.processScheduled");
      }
    }
  }

  /**
   * Attempts to process OgelSubmissions through each stage.
   * When stage is completed we set OgelSubmission to next stage and save OgelSubmission
   */
  private void doProcessOgelSubmission(OgelSubmission sub) {

    // Process Customer, Site and correct Role
    boolean customerStageComplete = hasCompletedStage(sub, OgelSubmission.Stage.CUSTOMER);
    boolean siteStageComplete = false;
    boolean roleUpdateStageComplete = false;

    // Process Customer
    if (!customerStageComplete) {
      customerStageComplete = submissionService.processForCustomer(sub);
      progressStage(sub); // update OgelSubmission to next stage
    }

    // Process Site
    if (customerStageComplete) {
      siteStageComplete = submissionService.processForSite(sub);
      progressStage(sub); // update OgelSubmission to next stage
    }

    // Process Role
    if (siteStageComplete) {
      roleUpdateStageComplete = submissionService.processForRoleUpdate(sub);
      progressStage(sub); // update OgelSubmission to next stage
    }

    // Process create Ogel
    if (customerStageComplete && siteStageComplete && roleUpdateStageComplete) {
      ogelService.processForOgel(sub);
    }
  }

  /**
   * Progresses OgelSubmission to its next (uncompleted) stage
   */
  private void progressStage(OgelSubmission sub) {
    OgelSubmission.Stage nextStage = getNextStage(sub.getStage());
    if (nextStage != null) {
      sub.setStage(nextStage);
      if (hasCompletedStage(sub, nextStage)) {
        progressStage(sub);
      } else {
        submissionDao.update(sub);
      }
    }
  }

  private OgelSubmission.Stage getNextStage(OgelSubmission.Stage stage) {
    OgelSubmission.Stage nextStage = null;
    if (stage.equals(OgelSubmission.Stage.CREATED)) {
      nextStage = OgelSubmission.Stage.CUSTOMER;
    } else if (stage.equals(OgelSubmission.Stage.CUSTOMER)) {
      nextStage = OgelSubmission.Stage.SITE;
    } else if (stage.equals(OgelSubmission.Stage.SITE)) {
      nextStage = OgelSubmission.Stage.USER_ROLE;
    } else if (stage.equals(OgelSubmission.Stage.USER_ROLE)) {
      nextStage = OgelSubmission.Stage.OGEL;
    }
    return nextStage;
  }

  private boolean hasCompletedStage(OgelSubmission sub, OgelSubmission.Stage stage) {
    boolean completed = false;
    if (stage.equals(OgelSubmission.Stage.CREATED)) {
      completed = true;
    } else if (stage.equals(OgelSubmission.Stage.CUSTOMER)) {
      completed = !Util.isBlank(sub.getCustomerRef());
    } else if (stage.equals(OgelSubmission.Stage.SITE)) {
      completed = !Util.isBlank(sub.getSiteRef());
    } else if (stage.equals(OgelSubmission.Stage.USER_ROLE)) {
      completed = !sub.isRoleUpdate() || sub.isRoleUpdated();
    } else if (stage.equals(OgelSubmission.Stage.OGEL)) {
      completed = !Util.isBlank(sub.getSpireRef());
    }
    return completed;
  }

  private void doCallbackOgelSubmission(OgelSubmission sub) {
    callbackService.completeCallback(sub);
  }

  private void errorThrown(OgelSubmission sub, Throwable e, String info) {
    String stackTrace = Throwables.getStackTraceAsString(e);
    failService.failWithMessage(sub, CallbackView.FailReason.UNCLASSIFIED, FailService.Origin.UNKNOWN, stackTrace);
    LOGGER.error(info + ": " + e.getMessage(), e);
  }
}

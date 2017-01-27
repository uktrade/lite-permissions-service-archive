package uk.gov.bis.lite.permissions.service;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

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
   * (This method is called by ProcessImmediateJob)
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
      String stackTrace = Throwables.getStackTraceAsString(e);
      failService.failWithMessage(sub, CallbackView.FailReason.UNCLASSIFIED, FailService.Origin.UNKNOWN, stackTrace);
      LOGGER.error("JobProcessService.processImmediate: " + e.getMessage(), e);
    }

  }

  public void processOgelSubmissions() {
    processScheduled();
    processCallbacks();
  }

  /**
   * Find scheduled OgelSubmissions and attempt to process each through all stages.
   * (This method is called by ProcessScheduledJob)
   */
  private void processScheduled() {
    List<OgelSubmission> subs = submissionDao.getScheduledActive();
    LOGGER.info("SCHEDULED ACTIVE [" + subs.size() + "]");
    for (OgelSubmission sub : subs) {
      try {
        doProcessOgelSubmission(sub);
      } catch (Throwable e) {
        String stackTrace = Throwables.getStackTraceAsString(e);
        failService.failWithMessage(sub, CallbackView.FailReason.UNCLASSIFIED, FailService.Origin.UNKNOWN, stackTrace);
        LOGGER.error("JobProcessService.processScheduled: " + e.getMessage(), e);
      }
    }
  }

  /**
   *
   */
  private void processCallbacks() {
    List<OgelSubmission> subs = submissionDao.getScheduledCompleteToCallback();
    LOGGER.info("SCHEDULED COMPLETE CALLBACK [" + subs.size() + "]");
    for (OgelSubmission sub : subs) {
      try {
        doCallbackOgelSubmission(sub);
      } catch (Throwable e) {
        String stackTrace = Throwables.getStackTraceAsString(e);
        failService.failWithMessage(sub, CallbackView.FailReason.UNCLASSIFIED, FailService.Origin.UNKNOWN, stackTrace);
        LOGGER.error("JobProcessService.processScheduled: " + e.getMessage(), e);
      }
    }
  }

  private void doCallbackOgelSubmission(OgelSubmission sub) {
    callbackService.completeCallback(sub);
  }

  /**
   * Attempts to complete all OgelSubmission stages
   * Delegate services responsible for updating OgelSubmission status, and reporting failures
   */
  private void doProcessOgelSubmission(OgelSubmission sub) {

    // Process Customer, Site and correct Role
    boolean customerStageComplete = submissionService.processForCustomer(sub);

    // Process Site
    boolean siteStageComplete = false;
    if (customerStageComplete) {
      siteStageComplete = submissionService.processForSite(sub);
    }

    // Process Role
    boolean roleUpdateStageComplete = false;
    if (siteStageComplete) {
      roleUpdateStageComplete = submissionService.processForRoleUpdate(sub);
    }

    // Process create Ogel
    if (customerStageComplete && siteStageComplete && roleUpdateStageComplete) {
      ogelService.processForOgel(sub);
    }
  }
}

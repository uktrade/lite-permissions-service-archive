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

      // Attempt to process this OgelSubmission immediately
      doProcessOgelSubmission(sub);

      // Update MODE if necessary
      submissionService.updateModeIfNotCompleted(sub.getId());

    } catch (Throwable e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      failService.failWithMessage(sub, CallbackView.FailReason.UNCLASSIFIED, FailService.Origin.UNKNOWN, stackTrace);
      LOGGER.error("JobProcessService.processImmediate: " + e.getMessage(), e);
    }

  }

  /**
   * Find scheduled OgelSubmissions and attempt to process each through all stages.
   * (This method is called by ProcessScheduledJob)
   */
  public void processScheduled() {
    List<OgelSubmission> subs = submissionDao.getScheduledToProcess();
    LOGGER.info("SCHEDULED [" + subs.size() + "]");

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
   * Attempts to complete all OgelSubmission stages
   * Delegate services responsible for updating OgelSubmission status, and reporting failures
   */
  private void doProcessOgelSubmission(OgelSubmission sub) {

    // Ensure we have Customer, Site and correct Role
    boolean preparedCustomer = submissionService.prepareCustomer(sub);

    boolean preparedSite = false;
    if (preparedCustomer) {
      preparedSite = submissionService.prepareSite(sub);
    }

    boolean preparedRoleUpdate = false;
    if (preparedSite) {
      preparedRoleUpdate = submissionService.prepareRoleUpdate(sub);
    }

    // Create Ogel
    if (preparedCustomer && preparedSite && preparedRoleUpdate) {
      if (!sub.isOgelCreated()) {
        ogelService.createOgel(sub);
      }
    }

    // Complete Callback
    if (sub.hasCompleted() && !sub.isCalledBack()) {
      callbackService.completeCallback(sub);
    }
  }
}

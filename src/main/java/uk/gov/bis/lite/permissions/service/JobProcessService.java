package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.List;

@Singleton
public class JobProcessService {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobProcessService.class);

  private OgelSubmissionDao submissionDao;
  private CustomerService customerService;
  private SubmissionService submissionService;
  private OgelService ogelService;
  private CallbackService callbackService;

  @Inject
  public JobProcessService(OgelSubmissionDao submissionDao, CustomerService customerService,
                           SubmissionService submissionService, OgelService ogelService, CallbackService callbackService) {
    this.submissionDao = submissionDao;
    this.customerService = customerService;
    this.submissionService = submissionService;
    this.ogelService = ogelService;
    this.callbackService = callbackService;
  }

  /**
   * Process OgelSubmission through all stages - set Mode to SCHEDULED if process cannot be completed
   * (This method is called by ProcessImmediateJob)
   */
  public void processImmediate(String submissionRef) {
    LOGGER.info("IMMEDIATE [" + submissionRef + "]");

    // Attempt to process this OgelSubmission immediately
    doProcessOgelSubmission(submissionRef);

    // Update MODE if necessary
    submissionService.updateModeIfNotCompleted(submissionRef);
  }

  /**
   * Find scheduled OgelSubmissions and attempt to process each through all stages.
   * (This method is called by ProcessScheduledJob)
   */
  public void processScheduled() {
    List<OgelSubmission> subs = submissionDao.getScheduledCallbacks();
    LOGGER.info("SCHEDULED [" + subs.size() + "]");
    subs.stream().map(OgelSubmission::getSubmissionRef).forEach(this::doProcessOgelSubmission);
  }

  /**
   * Attempts to complete all OgelSubmission stages
   * Delegate services responsible for updating OgelSubmission status, and reporting failures
   */
  private void doProcessOgelSubmission(String submissionRef) {

    OgelSubmission sub = submissionDao.findBySubmissionRef(submissionRef);

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

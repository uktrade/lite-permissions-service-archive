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
   * This method is called by ProcessImmediateJob
   */
  public void processImmediate(String submissionRef) {
    LOGGER.info("IMMEDIATE [" + submissionRef + "]");
    processOgelSubmission(submissionRef);
    submissionService.checkToResetMode(submissionRef); // Revise mode if necessary
  }

  /**
   * Find scheduled OgelSubmissions and attempt to process each through all stages.
   * This method is called by ProcessScheduledJob
   */
  public void processScheduled() {
    List<OgelSubmission> subs = submissionDao.getScheduledByStatus(OgelSubmission.Status.CUSTOMER.name());
    LOGGER.info("SCHEDULED [" + subs.size() + "]");
    subs.stream().map(OgelSubmission::getSubmissionRef).forEach(this::processOgelSubmission);
  }

  private void processOgelSubmission(String submissionRef) {
    boolean prepareOk = submissionService.prepareSubmission(submissionRef);
    if (prepareOk) {
      boolean createOk = ogelService.createOgel(submissionRef);
      if (createOk) {
        LOGGER.info("submissionRef [" + submissionRef + "] Create Ogel completed successfully.");
        callbackService.completeCallback(submissionRef);
      }
    }
  }
}

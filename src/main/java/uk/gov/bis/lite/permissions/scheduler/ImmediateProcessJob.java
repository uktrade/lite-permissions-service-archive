package uk.gov.bis.lite.permissions.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.service.CallbackService;
import uk.gov.bis.lite.permissions.service.OgelService;
import uk.gov.bis.lite.permissions.service.SubmissionService;

public class ImmediateProcessJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImmediateProcessJob.class);

  private SubmissionService submissionService;
  private OgelService ogelService;
  private CallbackService callbackService;
  private String subRef;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    init(context);
    LOGGER.info("ImmediateProcessJob submissionRef [" + subRef + "] started...");

    boolean prepareOk = submissionService.immediatePrepare(subRef);
    if (prepareOk) {
      boolean createOk = ogelService.immediateCreate(subRef);
      if (createOk) {
        LOGGER.info("ImmediateProcessJob submissionRef [" + subRef + "] completed successfully.");
        callbackService.completeCallback(subRef);
      }
    }

    // Revise mode if necessary
    submissionService.checkToResetMode(subRef);
  }

  private void init(JobExecutionContext context) {
    submissionService = (SubmissionService) context.getMergedJobDataMap().get(Scheduler.SUBMISSION_SERVICE_NAME);
    ogelService = (OgelService) context.getMergedJobDataMap().get(Scheduler.OGEL_SERVICE_NAME);
    callbackService = (CallbackService) context.getMergedJobDataMap().get(Scheduler.CLIENT_CALLBACK_SERVICE_NAME);
    subRef = (String) context.getMergedJobDataMap().get(Scheduler.SUBMISSION_REF);
  }
}

package uk.gov.bis.lite.permissions.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.service.OgelService;
import uk.gov.bis.lite.permissions.service.SubmissionService;

public class ImmediatePrepareCreateJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImmediatePrepareCreateJob.class);

  private SubmissionService submissionService;
  private OgelService ogelService;
  private String subRef;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    init(context);
    LOGGER.info("ImmediatePrepareCreateJob submissionRef [" + subRef + "] started...");

    if(submissionService.immediatePrepare(subRef)) {
      if(ogelService.immediateCreate(subRef)) {
        LOGGER.info("ImmediatePrepareCreateJob submissionRef [" + subRef + "] completed successfully.");
      }
    }

    // Set to SCHEDULED mode if necessary
    submissionService.scheduleIfNotComplete(subRef);
  }

  private void init(JobExecutionContext context) {
    submissionService = (SubmissionService) context.getMergedJobDataMap().get(PermissionsScheduler.SUBMISSION_SERVICE_NAME);
    ogelService = (OgelService) context.getMergedJobDataMap().get(PermissionsScheduler.OGEL_SERVICE_NAME);
    subRef = (String) context.getMergedJobDataMap().get(PermissionsScheduler.SUBMISSION_REF);
  }
}

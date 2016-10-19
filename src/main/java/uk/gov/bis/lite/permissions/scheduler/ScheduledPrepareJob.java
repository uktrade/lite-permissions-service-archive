package uk.gov.bis.lite.permissions.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.SubmissionService;

public class ScheduledPrepareJob implements Job {

  private SubmissionService submissionService;
  private static int cycle = 0;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    init(context);

    if (cycle == 1) {
      submissionService.processScheduled(OgelSubmission.Status.CUSTOMER);
    } else if (cycle == 2) {
      submissionService.processScheduled(OgelSubmission.Status.SITE);
    } else if (cycle == 3) {
      submissionService.processScheduled(OgelSubmission.Status.USER_ROLE);
    }
    cycle++;
    if (cycle > 3) {
      cycle = 1;
    }
  }

  private void init(JobExecutionContext context) {
    submissionService = (SubmissionService) context.getMergedJobDataMap().get(Scheduler.SUBMISSION_SERVICE_NAME);
  }
}

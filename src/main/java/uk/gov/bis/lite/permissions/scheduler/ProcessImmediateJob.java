package uk.gov.bis.lite.permissions.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.bis.lite.permissions.service.ProcessSubmissionService;

public class ProcessImmediateJob implements Job {

  private ProcessSubmissionService processSubmissionService;
  private long submissionId;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    init(context);
    processSubmissionService.processImmediate(submissionId);
  }

  private void init(JobExecutionContext context) {
    processSubmissionService = (ProcessSubmissionService) context.getMergedJobDataMap().get(Scheduler.JOB_PROCESS_SERVICE_NAME);
    submissionId = (long) context.getMergedJobDataMap().get(Scheduler.SUBMISSION_ID);
  }
}

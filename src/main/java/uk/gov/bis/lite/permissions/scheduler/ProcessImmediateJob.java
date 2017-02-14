package uk.gov.bis.lite.permissions.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.service.ProcessSubmissionService;

public class ProcessImmediateJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessImmediateJob.class);
  private ProcessSubmissionService processSubmissionService;
  private int submissionId;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    init(context);
    processSubmissionService.processImmediate(submissionId);
  }

  private void init(JobExecutionContext context) {
    processSubmissionService = (ProcessSubmissionService) context.getMergedJobDataMap().get(Scheduler.JOB_PROCESS_SERVICE_NAME);
    submissionId = (int) context.getMergedJobDataMap().get(Scheduler.SUBMISSION_ID);
  }
}

package uk.gov.bis.lite.permissions.scheduler;


import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.service.ProcessSubmissionService;

@DisallowConcurrentExecution
public class ProcessScheduledJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessScheduledJob.class);
  private ProcessSubmissionService processSubmissionService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    init(context);
    processSubmissionService.processOgelSubmissions();
  }

  private void init(JobExecutionContext context) {
    processSubmissionService = (ProcessSubmissionService) context.getMergedJobDataMap().get(Scheduler.JOB_PROCESS_SERVICE_NAME);
  }
}

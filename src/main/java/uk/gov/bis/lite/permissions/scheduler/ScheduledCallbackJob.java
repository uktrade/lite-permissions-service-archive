package uk.gov.bis.lite.permissions.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.bis.lite.permissions.service.CallbackService;

public class ScheduledCallbackJob implements Job {

  private CallbackService callbackService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    init(context);
    callbackService.completeScheduledCallbacks();
  }

  private void init(JobExecutionContext context) {
    callbackService = (CallbackService) context.getMergedJobDataMap().get(Scheduler.CLIENT_CALLBACK_SERVICE_NAME);
  }
}

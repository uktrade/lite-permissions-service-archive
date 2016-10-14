package uk.gov.bis.lite.permissions.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.bis.lite.permissions.service.OgelService;

public class OgelCreateJob implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    OgelService service = (OgelService) context.getMergedJobDataMap()
        .get(PermissionsScheduler.OGEL_SERVICE_NAME);

    service.doCreateOgels();
  }
}
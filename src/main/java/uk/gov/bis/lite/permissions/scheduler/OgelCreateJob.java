package uk.gov.bis.lite.permissions.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.service.OgelService;


public class OgelCreateJob  implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelCreateJob.class);

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    OgelService service = (OgelService) context.getMergedJobDataMap()
        .get(PermissionsScheduler.OGEL_SERVICE_NAME);

    service.doCreateOgels();
  }
}
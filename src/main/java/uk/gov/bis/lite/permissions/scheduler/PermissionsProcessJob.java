package uk.gov.bis.lite.permissions.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.service.RegisterService;

public class PermissionsProcessJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsProcessJob.class);

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    LOGGER.info("Starting PermissionsProcessJob...");
    RegisterService service = (RegisterService) context.getMergedJobDataMap()
        .get(PermissionsScheduler.OGEL_REG_SERVICE_NAME);

    //service.process();
  }
}

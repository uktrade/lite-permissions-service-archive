package uk.gov.bis.lite.permissions.scheduler;

import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;
import uk.gov.bis.lite.permissions.service.RegisterService;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class PermissionsScheduler implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsScheduler.class);

  private final Scheduler scheduler;
  private final PermissionsAppConfig config;
  private final RegisterService registerService;
  public static final String OGEL_REG_SERVICE_NAME = "registerService";

  @Inject
  public PermissionsScheduler(Scheduler scheduler, PermissionsAppConfig config,
                              RegisterService registerService) {
    this.scheduler = scheduler;
    this.config = config;
    this.registerService = registerService;
  }

  @Override
  public void start() throws Exception {
    LOGGER.info("PermissionsScheduler start...");

    JobKey jobKey = JobKey.jobKey("notificationJob");
    JobDetail jobDetail = newJob(PermissionsProcessJob.class)
        .withIdentity(jobKey)
        .build();

    jobDetail.getJobDataMap().put(OGEL_REG_SERVICE_NAME, registerService);

    CronTrigger trigger = newTrigger()
        .withIdentity(TriggerKey.triggerKey("notificationRetryJobTrigger"))
        .withSchedule(cronSchedule(config.getNotificationRetryJobCron()))
        .build();

    scheduler.scheduleJob(jobDetail, trigger);
    scheduler.start();
    scheduler.triggerJob(jobKey);
  }

  @Override
  public void stop() throws Exception {
    scheduler.shutdown(true);
  }
}

package uk.gov.bis.lite.permissions.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;
import uk.gov.bis.lite.permissions.service.ProcessOgelSubmissionService;

public class Scheduler implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

  private final org.quartz.Scheduler scheduler;
  private final PermissionsAppConfig config;
  private ProcessOgelSubmissionService processOgelSubmissionService;

  public static final String JOB_PROCESS_SERVICE_NAME = "processOgelSubmissionService";
  public static final String SUBMISSION_ID = "SUBMISSION_ID";

  @Inject
  public Scheduler(org.quartz.Scheduler scheduler, PermissionsAppConfig config, ProcessOgelSubmissionService processOgelSubmissionService) {
    this.scheduler = scheduler;
    this.config = config;
    this.processOgelSubmissionService = processOgelSubmissionService;
  }

  @Override
  public void start() throws Exception {
    LOGGER.info("Scheduler start...");

    // Set up ProcessScheduledJob
    JobKey key = JobKey.jobKey("ProcessScheduledJob");
    JobDetail detail = newJob(ProcessScheduledJob.class).withIdentity(key).build();
    detail.getJobDataMap().put(JOB_PROCESS_SERVICE_NAME, processOgelSubmissionService);
    CronTrigger trigger = newTrigger()
        .withIdentity(TriggerKey.triggerKey("ProcessScheduledJobTrigger"))
        .withSchedule(cronSchedule(config.getProcessScheduledJobCron()))
        .build();

    scheduler.scheduleJob(detail, trigger);
    scheduler.triggerJob(key);
    scheduler.startDelayed(3); // to avoid premature process on startup
  }

  @Override
  public void stop() throws Exception {
    scheduler.shutdown(true);
  }
}

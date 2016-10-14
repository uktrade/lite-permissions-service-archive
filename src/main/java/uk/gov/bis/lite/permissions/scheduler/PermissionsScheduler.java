package uk.gov.bis.lite.permissions.scheduler;

import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;
import uk.gov.bis.lite.permissions.service.OgelService;
import uk.gov.bis.lite.permissions.service.SubmissionService;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class PermissionsScheduler implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsScheduler.class);

  private final Scheduler scheduler;
  private final PermissionsAppConfig config;
  private final SubmissionService submissionService;
  private final OgelService ogelService;

  static final String SUBMISSION_SERVICE_NAME = "submissionService";
  static final String OGEL_SERVICE_NAME = "ogelService";

  @Inject
  public PermissionsScheduler(Scheduler scheduler, PermissionsAppConfig config,
                              SubmissionService submissionService, OgelService ogelService) {
    this.scheduler = scheduler;
    this.config = config;
    this.submissionService = submissionService;
    this.ogelService = ogelService;
  }

  @Override
  public void start() throws Exception {
    LOGGER.info("PermissionsScheduler start...");

    // Set up OgelSubmission prepare job
    JobKey prepareJobKey = JobKey.jobKey("OgelSubmissionPrepareJob");
    JobDetail prepareJobDetail = newJob(OgelSubmissionPrepareJob.class)
        .withIdentity(prepareJobKey)
        .build();
    prepareJobDetail.getJobDataMap().put(SUBMISSION_SERVICE_NAME, submissionService);
    CronTrigger prepareTrigger = newTrigger()
        .withIdentity(TriggerKey.triggerKey("OgelSubmissionPrepareJobTrigger"))
        .withSchedule(cronSchedule(config.getOgelPrepareJobCron()))
        .build();

    // Set up Ogel create job
    JobKey createJobKey = JobKey.jobKey("OgelCreateJob");
    JobDetail createJobDetail = newJob(OgelCreateJob.class)
        .withIdentity(createJobKey)
        .build();
    createJobDetail.getJobDataMap().put(OGEL_SERVICE_NAME, ogelService);
    CronTrigger createTrigger = newTrigger()
        .withIdentity(TriggerKey.triggerKey("OgelCreateJobTrigger"))
        .withSchedule(cronSchedule(config.getOgelCreateJobCron()))
        .build();

    scheduler.scheduleJob(prepareJobDetail, prepareTrigger);
    scheduler.scheduleJob(createJobDetail, createTrigger);

    scheduler.start();
    scheduler.triggerJob(prepareJobKey);
    scheduler.triggerJob(createJobKey);
  }

  @Override
  public void stop() throws Exception {
    scheduler.shutdown(true);
  }
}

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
import uk.gov.bis.lite.permissions.service.CallbackService;
import uk.gov.bis.lite.permissions.service.OgelService;
import uk.gov.bis.lite.permissions.service.SubmissionService;

public class Scheduler implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

  private final org.quartz.Scheduler scheduler;
  private final PermissionsAppConfig config;
  private final SubmissionService submissionService;
  private final OgelService ogelService;
  private final CallbackService callbackService;

  public static final String SUBMISSION_SERVICE_NAME = "submissionService";
  public static final String OGEL_SERVICE_NAME = "ogelService";
  public static final String CLIENT_CALLBACK_SERVICE_NAME = "callbackService";
  public static final String SUBMISSION_REF = "SUBMISSION_REF";

  @Inject
  public Scheduler(org.quartz.Scheduler scheduler, PermissionsAppConfig config, SubmissionService submissionService,
                   OgelService ogelService, CallbackService callbackService) {
    this.scheduler = scheduler;
    this.config = config;
    this.submissionService = submissionService;
    this.ogelService = ogelService;
    this.callbackService = callbackService;
  }

  @Override
  public void start() throws Exception {
    LOGGER.info("Scheduler start...");

    // Set up OgelSubmission prepare job
    JobKey prepareKey = JobKey.jobKey("ScheduledPrepareJob");
    JobDetail prepareDetail = newJob(ScheduledPrepareJob.class).withIdentity(prepareKey).build();
    prepareDetail.getJobDataMap().put(SUBMISSION_SERVICE_NAME, submissionService);
    CronTrigger prepareTrigger = newTrigger()
        .withIdentity(TriggerKey.triggerKey("ScheduledPrepareJobTrigger"))
        .withSchedule(cronSchedule(config.getScheduledPrepareJobCron()))
        .build();

    // Set up Ogel create job
    JobKey createKey = JobKey.jobKey("ScheduledCreateJob");
    JobDetail createDetail = newJob(ScheduledCreateJob.class).withIdentity(createKey).build();
    createDetail.getJobDataMap().put(OGEL_SERVICE_NAME, ogelService);
    CronTrigger createTrigger = newTrigger()
        .withIdentity(TriggerKey.triggerKey("ScheduledCreateTrigger"))
        .withSchedule(cronSchedule(config.getScheduledCreateJobCron()))
        .build();

    // Set up Callback job
    JobKey callbackKey = JobKey.jobKey("ScheduledCallbackJob");
    JobDetail callbackDetail = newJob(ScheduledCallbackJob.class).withIdentity(callbackKey).build();
    callbackDetail.getJobDataMap().put(CLIENT_CALLBACK_SERVICE_NAME, callbackService);
    CronTrigger callbackTrigger = newTrigger()
        .withIdentity(TriggerKey.triggerKey("ScheduledCallbackJobTrigger"))
        .withSchedule(cronSchedule(config.getScheduledCallbackJobCron()))
        .build();

    scheduler.scheduleJob(prepareDetail, prepareTrigger);
    scheduler.scheduleJob(createDetail, createTrigger);
    scheduler.scheduleJob(callbackDetail, callbackTrigger);

    scheduler.start();

    scheduler.triggerJob(prepareKey);
    scheduler.triggerJob(createKey);
    scheduler.triggerJob(callbackKey);
  }

  @Override
  public void stop() throws Exception {
    scheduler.shutdown(true);
  }
}

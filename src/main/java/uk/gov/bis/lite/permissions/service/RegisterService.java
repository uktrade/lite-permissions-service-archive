package uk.gov.bis.lite.permissions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.model.register.RegisterOgel;
import uk.gov.bis.lite.permissions.scheduler.ImmediateProcessJob;
import uk.gov.bis.lite.permissions.scheduler.Scheduler;

@Singleton
public class RegisterService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterService.class);

  private OgelSubmissionDao submissionDao;
  private final org.quartz.Scheduler scheduler;
  private final SubmissionService submissionService;
  private final OgelService ogelService;
  private final CallbackService callbackService;

  @Inject
  public RegisterService(OgelSubmissionDao submissionDao, org.quartz.Scheduler scheduler, SubmissionService submissionService,
                         OgelService ogelService, CallbackService callbackService) {
    this.submissionDao = submissionDao;
    this.scheduler = scheduler;
    this.submissionService = submissionService;
    this.ogelService = ogelService;
    this.callbackService = callbackService;
  }

  /**
   * Creates and persists OgelSubmission in IMMEDIATE mode
   * Triggers a ImmediateProcessJob job to process submission
   */
  public String register(RegisterOgel reg, String callbackUrl) {
    LOGGER.info("Creating OgelSubmission: " + reg.getUserId() + "/" + reg.getOgelType());

    // Create new OgelSubmission and persist
    OgelSubmission sub = getOgelSubmission(reg);
    sub.setCallbackUrl(callbackUrl);
    sub.setMode(OgelSubmission.Mode.IMMEDIATE);
    sub.setStatus(OgelSubmission.Status.CREATED);

    submissionDao.create(sub);

    // Trigger job to process submission
    triggerProcessSubmissionJob(sub.getSubmissionRef());

    return sub.getSubmissionRef();
  }

  private void triggerProcessSubmissionJob(String subRef) {
    JobDetail detail = JobBuilder.newJob(ImmediateProcessJob.class).build();
    JobDataMap dataMap = detail.getJobDataMap();
    dataMap.put(Scheduler.SUBMISSION_SERVICE_NAME, submissionService);
    dataMap.put(Scheduler.OGEL_SERVICE_NAME, ogelService);
    dataMap.put(Scheduler.CLIENT_CALLBACK_SERVICE_NAME, callbackService);
    dataMap.put(Scheduler.SUBMISSION_REF, subRef);
    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(TriggerKey.triggerKey("SubmissionProcessJobTrigger-" + subRef))
        .startNow().build();
    try {
      scheduler.scheduleJob(detail, trigger);
    } catch (SchedulerException e) {
      LOGGER.error("SchedulerException", e);
    }
  }

  private OgelSubmission getOgelSubmission(RegisterOgel reg) {
    ObjectMapper mapper = new ObjectMapper();
    OgelSubmission sub = new OgelSubmission(reg.getUserId(), reg.getOgelType());
    sub.setCustomerRef(reg.getExistingCustomer());
    sub.setSiteRef(reg.getExistingSite());
    sub.setSubmissionRef(reg.generateSubmissionReference());
    sub.setRoleUpdate(reg.isRoleUpdateRequired());
    sub.setCalledBack(false);
    try {
      sub.setJson(mapper.writeValueAsString(reg).replaceAll("\\s{2,}", " ").trim()); // remove excessive whitespace
    } catch (JsonProcessingException e) {
      LOGGER.error("JsonProcessingException", e);
    }
    return sub;
  }
}

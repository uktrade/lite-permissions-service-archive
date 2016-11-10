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
import uk.gov.bis.lite.permissions.scheduler.ProcessImmediateJob;
import uk.gov.bis.lite.permissions.scheduler.Scheduler;

@Singleton
public class RegisterServiceImpl implements RegisterService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterServiceImpl.class);

  private OgelSubmissionDao submissionDao;
  private org.quartz.Scheduler scheduler;
  private JobProcessService jobProcessService;
  private ObjectMapper mapper;

  @Inject
  public RegisterServiceImpl(OgelSubmissionDao submissionDao, org.quartz.Scheduler scheduler, JobProcessService jobProcessService) {
    this.submissionDao = submissionDao;
    this.scheduler = scheduler;
    this.jobProcessService = jobProcessService;
    this.mapper = new ObjectMapper();
  }

  /**
   * Creates and persists OgelSubmission in IMMEDIATE mode
   * Triggers a ProcessImmediateJob job to process submission
   */
  public String register(RegisterOgel reg, String callbackUrl) {
    LOGGER.info("Creating OgelSubmission: " + reg.getUserId() + "/" + reg.getOgelType());

    // Create new OgelSubmission and persist
    OgelSubmission sub = getOgelSubmission(reg);
    sub.setCallbackUrl(callbackUrl);
    sub.setMode(OgelSubmission.Mode.IMMEDIATE);
    sub.setStatus(OgelSubmission.Status.CREATED);

    submissionDao.create(sub);

    // Trigger ProcessImmediateJob to process this submission
    triggerProcessSubmissionJob(sub.getSubmissionRef());

    return sub.getSubmissionRef();
  }

  private void triggerProcessSubmissionJob(String submissionRef) {
    JobDetail detail = JobBuilder.newJob(ProcessImmediateJob.class).build();
    JobDataMap dataMap = detail.getJobDataMap();
    dataMap.put(Scheduler.JOB_PROCESS_SERVICE_NAME, jobProcessService);
    dataMap.put(Scheduler.SUBMISSION_REF, submissionRef);
    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(TriggerKey.triggerKey("SubmissionProcessJobTrigger-" + submissionRef))
        .startNow().build();
    try {
      scheduler.scheduleJob(detail, trigger);
    } catch (SchedulerException e) {
      LOGGER.error("SchedulerException", e);
    }
  }

  private OgelSubmission getOgelSubmission(RegisterOgel reg) {
    OgelSubmission sub = new OgelSubmission(reg.getUserId(), reg.getOgelType());
    sub.setCustomerRef(reg.getExistingCustomer());
    sub.setSiteRef(reg.getExistingSite());
    sub.setSubmissionRef(reg.generateSubmissionReference());
    sub.setRoleUpdate(reg.isRoleUpdateRequired());
    sub.setCalledBack(false);
    try {
      sub.setJson(mapper.writeValueAsString(reg));
    } catch (JsonProcessingException e) {
      LOGGER.error("JsonProcessingException", e);
    }
    return sub;
  }
}

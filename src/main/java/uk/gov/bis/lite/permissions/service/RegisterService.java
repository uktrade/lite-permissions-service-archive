package uk.gov.bis.lite.permissions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.model.register.RegisterOgel;
import uk.gov.bis.lite.permissions.scheduler.ImmediatePrepareCreateJob;
import uk.gov.bis.lite.permissions.scheduler.PermissionsScheduler;

@Singleton
public class RegisterService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterService.class);

  private OgelSubmissionDao submissionDao;
  private final Scheduler scheduler;
  private final SubmissionService submissionService;
  private final OgelService ogelService;

  @Inject
  public RegisterService(OgelSubmissionDao submissionDao, Scheduler scheduler, SubmissionService submissionService,
                         OgelService ogelService) {
    this.submissionDao = submissionDao;
    this.scheduler = scheduler;
    this.submissionService = submissionService;
    this.ogelService = ogelService;
  }

  public String register(RegisterOgel reg) {
    LOGGER.info("Creating OgelSubmission: " + reg.getUserId() + "/" + reg.getOgelType());

    OgelSubmission sub = getOgelSubmission(reg);
    sub.setMode(OgelSubmission.Mode.IMMEDIATE);
    sub.setStatus(OgelSubmission.Status.CREATED);

    submissionDao.create(sub);

    String subRef = sub.getSubmissionRef();
    createSubmissionProcessJob(subRef);
    return subRef;
  }

  private void createSubmissionProcessJob(String subRef) {
    JobDetail jobDetail = JobBuilder.newJob(ImmediatePrepareCreateJob.class).build();
    jobDetail.getJobDataMap().put(PermissionsScheduler.SUBMISSION_SERVICE_NAME, submissionService);
    jobDetail.getJobDataMap().put(PermissionsScheduler.OGEL_SERVICE_NAME, ogelService);
    jobDetail.getJobDataMap().put(PermissionsScheduler.SUBMISSION_REF, subRef);
    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(TriggerKey.triggerKey("SubmissionProcessJobTrigger"))
        .startNow().build();
    try {
      scheduler.scheduleJob(jobDetail, trigger);
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
    try {
      sub.setJson(mapper.writeValueAsString(reg).replaceAll("\\s{2,}", " ").trim()); // remove excessive whitespace
    } catch (JsonProcessingException e) {
      LOGGER.error("JsonProcessingException", e);
    }
    return sub;
  }
}

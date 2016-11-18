package uk.gov.bis.lite.permissions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.api.param.RegisterAddressParam;
import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.scheduler.ProcessImmediateJob;
import uk.gov.bis.lite.permissions.scheduler.Scheduler;
import uk.gov.bis.lite.permissions.util.Util;

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
   * Returns the requestId associated with the submission
   */
  public String register(RegisterParam reg, String callbackUrl) {
    LOGGER.info("Creating OgelSubmission: " + reg.getUserId() + "/" + reg.getOgelType());

    // Create new OgelSubmission and persist
    OgelSubmission sub = getOgelSubmission(reg);
    sub.setCallbackUrl(callbackUrl);
    sub.setMode(OgelSubmission.Mode.IMMEDIATE);
    sub.setStatus(OgelSubmission.Status.CREATED);

    int submissionId = submissionDao.create(sub);

    // Trigger ProcessImmediateJob to process this submission
    triggerProcessSubmissionJob(submissionId);

    sub.setId(submissionId); // set temporarily (id not set on object during create dao process) so we can then extract requestId
    return sub.getRequestId();
  }

  /**
   * Gathers data, creates  hash
   */
  public String generateSubmissionReference(RegisterParam registerParam) {
    String message = registerParam.joinedInstanceStateData().replaceAll("\\s+", "").toUpperCase();
    return Util.generateHashFromString(message);
  }

  /**
   * Determines whether the RegisterParam is valid or not
   */
  public boolean isRegisterParamValid(RegisterParam param) {

    // Check mandatory, customer and site fields are valid
    boolean valid = param.mandatoryFieldsOk() && param.customerFieldsOk() && param.siteFieldsOk();

    // If valid we also check if site address/name is valid
    if (valid && param.hasNewSite()) {
      RegisterParam.RegisterSiteParam siteParam = param.getNewSite();
      if (siteParam.isUseCustomerAddress() && param.hasNewCustomer()) {
        valid = registerAddressParamValid(param.getNewCustomer().getRegisteredAddress());
      } else {
        valid = !StringUtils.isBlank(siteParam.getSiteName()) && registerAddressParamValid(siteParam.getAddress());
      }
    }

    return valid;
  }

  /**
   * Return information on any validity errors within RegisterParam
   */
  public String getRegisterParamValidationInfo(RegisterParam param) {

    String info = !param.mandatoryFieldsOk() ? "Fields are mandatory: userId, ogelType. " : "";
    String customerCheck = !param.customerFieldsOk() ? "Must have existing Customer or new Customer fields. " : "";
    String siteCheck = !param.siteFieldsOk() ? "Must have existing Site or new Site fields. " : "";
    info = info + customerCheck + siteCheck;

    if (param.hasNewSite()) {
      RegisterParam.RegisterSiteParam siteParam = param.getNewSite();
      if (siteParam.isUseCustomerAddress() && param.hasNewCustomer()) {
        if (!registerAddressParamValid(param.getNewCustomer().getRegisteredAddress())) {
          info = info + " New Site must specify the country and one other address component. ";
        }
      } else {
        if (StringUtils.isBlank(siteParam.getSiteName())) {
          info = info + " New Site must have a site name ('siteName'). ";
        }
        if (!registerAddressParamValid(siteParam.getAddress())) {
          info = info + " New Site must specify the country and one other address component. ";
        }
      }
    }
    return info;
  }

  private void triggerProcessSubmissionJob(int submissionId) {
    JobDetail detail = JobBuilder.newJob(ProcessImmediateJob.class).build();
    JobDataMap dataMap = detail.getJobDataMap();
    dataMap.put(Scheduler.JOB_PROCESS_SERVICE_NAME, jobProcessService);
    dataMap.put(Scheduler.SUBMISSION_ID, submissionId);
    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(TriggerKey.triggerKey("SubmissionProcessJobTrigger-" + submissionId))
        .startNow().build();
    try {
      scheduler.scheduleJob(detail, trigger);
    } catch (SchedulerException e) {
      LOGGER.error("SchedulerException", e);
    }
  }

  private OgelSubmission getOgelSubmission(RegisterParam param) {
    OgelSubmission sub = new OgelSubmission(param.getUserId(), param.getOgelType());
    sub.setCustomerRef(param.getExistingCustomer());
    sub.setSiteRef(param.getExistingSite());
    sub.setSubmissionRef(generateSubmissionReference(param));
    sub.setRoleUpdate(param.roleUpdateRequired());
    sub.setCalledBack(false);
    try {
      sub.setJson(mapper.writeValueAsString(param));
    } catch (JsonProcessingException e) {
      LOGGER.error("JsonProcessingException", e);
    }
    return sub;
  }

  /**
   * Address must be non-null, country must be specified,
   * and at least one part of address must be not null/blank - to be valid
   */
  private boolean registerAddressParamValid(RegisterAddressParam param) {
    boolean valid = false;
    if (param != null && !StringUtils.isBlank(param.getCountry())) {
      if (!StringUtils.isBlank(param.getLine1()) || !StringUtils.isBlank(param.getLine2()) || !StringUtils.isBlank(param.getTown())
          || !StringUtils.isBlank(param.getPostcode()) || !StringUtils.isBlank(param.getCounty())) {
        valid = true;
      }
    }
    return valid;
  }
}

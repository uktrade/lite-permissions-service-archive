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

import java.util.ArrayList;
import java.util.List;

@Singleton
public class RegisterServiceImpl implements RegisterService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterServiceImpl.class);

  private OgelSubmissionDao submissionDao;
  private org.quartz.Scheduler scheduler;
  private ProcessSubmissionService processSubmissionService;
  private ObjectMapper mapper;

  @Inject
  public RegisterServiceImpl(OgelSubmissionDao submissionDao, org.quartz.Scheduler scheduler, ProcessSubmissionService processSubmissionService) {
    this.submissionDao = submissionDao;
    this.scheduler = scheduler;
    this.processSubmissionService = processSubmissionService;
    this.mapper = new ObjectMapper();
  }

  /**
   * Returns new OgelSubmission from RegisterParam
   */
  public OgelSubmission getOgelSubmission(RegisterParam param) {
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
    if (param.getAdminApproval() != null) {
      String adminUserId = param.getAdminApproval().getAdminUserId();
      if (!StringUtils.isBlank(adminUserId)) {
        sub.setAdminUserId(adminUserId);
      }
    }
    return sub;
  }

  /**
   * Creates and persists OgelSubmission in IMMEDIATE mode
   * Triggers a ProcessImmediateJob job to process submission
   * Returns the requestId associated with the submission
   */
  public String register(OgelSubmission sub, String callbackUrl) {
    LOGGER.info("Registering OgelSubmission UserID[{}] OgelType[{}]", sub.getUserId(), sub.getOgelType());

    // Persist OgelSubmission
    sub.setCallbackUrl(callbackUrl);
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
    String data = getDataStringFromRegisterParam(registerParam);
    return Util.generateHashFromString(data.replaceAll("\\s+", "").toUpperCase());
  }

  /**
   * Determines whether the RegisterParam is valid or not
   */
  public boolean isRegisterParamValid(RegisterParam param) {
    List<String> invalidParamMsg = checkValidRegisterParam(param);
    return invalidParamMsg.isEmpty();
  }

  /**
   * Return information on any validity errors within RegisterParam
   */
  public String getRegisterParamValidationInfo(RegisterParam param) {
    return checkValidRegisterParam(param).toString();
  }

  /**
   * RegisterParam validation
   */
  private List<String> checkValidRegisterParam(RegisterParam param) {
    List<String> invalidParamMsg = new ArrayList<>();

    // Check mandatory, customer and site fields are valid
    if (!param.mandatoryFieldsOk()) {
      invalidParamMsg.add("Fields are mandatory: userId, ogelType.");
    }
    if (!param.customerFieldsOk()) {
      invalidParamMsg.add("Must have existing Customer or new Customer fields.");
    }
    if (!param.siteFieldsOk()) {
      invalidParamMsg.add("Must have existing Site or new Site fields.");
    }

    if (invalidParamMsg.isEmpty() && param.hasNewSite()) {
      RegisterParam.RegisterSiteParam siteParam = param.getNewSite();
      if (param.hasNewCustomer()) {
        if (StringUtils.isBlank(siteParam.getSiteName())) {
          invalidParamMsg.add("New Site must have a site name ('siteName').");
        }
        if (siteParam.isUseCustomerAddress()) {
          if (!registerAddressParamValid(param.getNewCustomer().getRegisteredAddress())) {
            invalidParamMsg.add("New Site must specify the country and one other address component.");
          }
        } else if (!registerAddressParamValid(siteParam.getAddress())) {
          invalidParamMsg.add("New Site must specify the country and one other address component.");
        }
      }
    }
    if (param.hasExistingSite() && param.hasNewCustomer()) {
      invalidParamMsg.add(" Cannot have an existing Site for a new Customer.");
    }
    return invalidParamMsg;
  }

  /**
   * Extracts and returns data from RegisterParam as a string
   */
  private String getDataStringFromRegisterParam(RegisterParam param) {
    String registerString = StringUtils.join(param.getUserId(), param.getOgelType(), param.getExistingCustomer(), param.getExistingSite());

    // Customer data
    String customerString = "";
    if (param.hasNewCustomer()) {
      RegisterParam.RegisterCustomerParam customer = param.getNewCustomer();
      customerString = StringUtils.join(customer.getCustomerName(), customer.getCustomerType(), customer.getChNumber(),
          customer.getEoriNumber(), customer.getWebsite());
      customerString = customerString + StringUtils.join(customer.isChNumberValidated(), customer.isEoriNumberValidated());
      RegisterAddressParam address = customer.getRegisteredAddress();
      if (address != null) {
        customerString = customerString + StringUtils.join("", address.getLine1(), address.getLine2(), address.getTown(), address.getCounty(), address.getPostcode(), address.getCountry());
      }
    }

    // Site data
    String siteString = "";
    if (param.hasNewSite()) {
      RegisterParam.RegisterSiteParam site = param.getNewSite();
      siteString = site.getSiteName() + site.isUseCustomerAddress();
      RegisterAddressParam address = site.getAddress();
      if (address != null) {
        siteString = siteString + StringUtils.join("", address.getLine1(), address.getLine2(), address.getTown(), address.getCounty(), address.getPostcode(), address.getCountry());
      }
    }

    // Admin approval data
    String adminString = "";
    RegisterParam.RegisterAdminApprovalParam admin = param.getAdminApproval();
    if (admin != null && !StringUtils.isBlank(admin.getAdminUserId())) {
      adminString = admin.getAdminUserId();
    }

    // Concat all and return
    return registerString + customerString + siteString + adminString;
  }

  private void triggerProcessSubmissionJob(int submissionId) {
    JobDetail detail = JobBuilder.newJob(ProcessImmediateJob.class).build();
    JobDataMap dataMap = detail.getJobDataMap();
    dataMap.put(Scheduler.JOB_PROCESS_SERVICE_NAME, processSubmissionService);
    dataMap.put(Scheduler.SUBMISSION_ID, submissionId);
    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(TriggerKey.triggerKey("SubmissionProcessJobTrigger-" + submissionId))
        .startNow().build();
    try {
      scheduler.scheduleJob(detail, trigger);
    } catch (SchedulerException e) {
      LOGGER.error("SchedulerException SubID[" + submissionId + "]", e);
    }
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

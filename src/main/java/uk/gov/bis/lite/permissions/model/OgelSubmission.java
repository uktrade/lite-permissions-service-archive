package uk.gov.bis.lite.permissions.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.util.Util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OgelSubmission {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelSubmission.class);

  private int id;
  private String userId;
  private String ogelType;
  private Mode mode;
  private Status status;
  private String submissionRef;
  private String customerRef;
  private String siteRef;
  private String spireRef;
  private String firstFail;
  private String lastFailMessage;
  private String callbackUrl;
  private boolean calledBack;
  private String json;
  private String created;
  private boolean roleUpdate;
  private boolean roleUpdated;

  private static DateTimeFormatter ogelSubmissionDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /**
   * IMMEDIATE      - submission is being processed immediately, through all stages
   * SCHEDULED      - submission is to processed by scheduled jobs
   */
  public enum Mode {
    IMMEDIATE, SCHEDULED;
  }

  /**
   * CREATED    - initial status on creation
   * CUSTOMER   - we need to create a Customer and populate customerId with resulting sarRef
   * SITE       - we need to create a Site and populate siteId with resulting siteRef
   * USER_ROLE  - we need to update user role permissions
   * READY      - this OgelSubmission is now setUp and we can create the Ogel via Spire
   * SUCCESS    - Registration completed on Spire, OgelSubmission updated with Spire Ref, processing submission complete
   * ERROR      - Unresolved repeating error, processing submission terminated
   */
  public enum Status {
    CREATED, CUSTOMER, SITE, USER_ROLE, READY, SUCCESS, ERROR;
  }

  public OgelSubmission(int id) {
    this.id = id;
  }

  public OgelSubmission(String userId, String ogelType) {
    this.userId = userId;
    this.ogelType = ogelType;
    this.mode = Mode.IMMEDIATE;
    this.status = Status.CREATED;
  }

  /**
   * RequestId is used to identify a submission to external services. It is made up
   * from the submissionRef plus the id
   */
  public String getRequestId() {
    return submissionRef + "_" + id;
  }

  public boolean isModeScheduled() {
    return mode.equals(OgelSubmission.Mode.SCHEDULED);
  }

  public boolean isModeImmediate() {
    return mode.equals(OgelSubmission.Mode.IMMEDIATE);
  }

  public boolean isStatusSuccess() {
    return status.equals(Status.SUCCESS);
  }

  public boolean isStatusError() {
    return status.equals(Status.ERROR);
  }

  public boolean hasCompleted() {
    return isStatusSuccess() || isStatusError();
  }

  public boolean canCreateOgel() {
    return !needsCustomer() && !needsSite() && !needsRoleUpdate();
  }

  public boolean isOgelCreated() {
    return status.equals(Status.SUCCESS);
  }

  public boolean hasFail() {
    return !StringUtils.isBlank(firstFail);
  }

  public LocalDateTime getFirstFailDateTime() {
    LocalDateTime date = null;
    if (!StringUtils.isBlank(firstFail)) {
      date = LocalDateTime.parse(firstFail, ogelSubmissionDateFormatter);
    }
    return date;
  }

  public void setFirstFailDateTime() {
    LocalDateTime now = LocalDateTime.now();
    firstFail = now.format(ogelSubmissionDateFormatter);
  }

  public void changeToScheduledMode() {
    mode = Mode.SCHEDULED;
  }

  public void updateStatusToSuccess() {
    status = Status.SUCCESS;
  }

  public void updateStatusToError() {
    status = Status.ERROR;
  }

  /**
   * Sets appropriate Status value
   * - only if current STATUS is not READY or if OgelSubmission has not completed
   */
  public void updateStatus() {
    if (!status.equals(Status.READY) && !hasCompleted()) {
      if (needsCustomer()) {
        status = Status.CUSTOMER;
      } else if (needsSite()) {
        status = Status.SITE;
      } else if (needsRoleUpdate()) {
        status = Status.USER_ROLE;
      } else {
        status = Status.READY;
      }
    }
  }

  public boolean needsCustomer() {
    return Util.isBlank(customerRef);
  }

  public boolean needsSite() {
    return Util.isBlank(siteRef);
  }

  public boolean needsRoleUpdate() {
    return roleUpdate && !roleUpdated;
  }

  public RegisterParam getRegisterParamFromJson() {
    RegisterParam param = null;
    ObjectMapper mapper = new ObjectMapper();
    try {
      param = mapper.readValue(this.getJson(), RegisterParam.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return param;
  }

  public String getJson() {
    return json;
  }

  public void setJson(String json) {
    this.json = json;
  }

  public String getSubmissionRef() {
    return submissionRef;
  }

  public void setSubmissionRef(String submissionRef) {
    this.submissionRef = submissionRef;
  }

  public String getCustomerRef() {
    return customerRef;
  }

  public void setCustomerRef(String customerRef) {
    this.customerRef = customerRef;
  }

  public String getSiteRef() {
    return siteRef;
  }

  public void setSiteRef(String siteRef) {
    this.siteRef = siteRef;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getOgelType() {
    return ogelType;
  }

  public void setOgelType(String ogelType) {
    this.ogelType = ogelType;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getCreated() {
    return created;
  }

  public void setCreated(String created) {
    this.created = created;
  }

  public boolean isRoleUpdate() {
    return roleUpdate;
  }

  public void setRoleUpdate(boolean roleUpdate) {
    this.roleUpdate = roleUpdate;
  }

  public String getSpireRef() {
    return spireRef;
  }

  public void setSpireRef(String spireRef) {
    this.spireRef = spireRef;
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  public boolean isRoleUpdated() {
    return roleUpdated;
  }

  public void setRoleUpdated(boolean roleUpdated) {
    this.roleUpdated = roleUpdated;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

  public boolean isCalledBack() {
    return calledBack;
  }

  public void setCalledBack(boolean calledBack) {
    this.calledBack = calledBack;
  }

  public String getFirstFail() {
    return firstFail;
  }

  public void setFirstFail(String firstFail) {
    this.firstFail = firstFail;
  }

  public String getLastFailMessage() {
    return lastFailMessage;
  }

  public void setLastFailMessage(String lastFailMessage) {
    this.lastFailMessage = lastFailMessage;
  }
}

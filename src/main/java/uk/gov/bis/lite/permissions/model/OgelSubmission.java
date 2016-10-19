package uk.gov.bis.lite.permissions.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.model.register.RegisterOgel;
import uk.gov.bis.lite.permissions.util.Util;

import java.io.IOException;

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
  private String callbackUrl;
  private boolean calledBack;
  private String json;
  private String created;
  private boolean roleUpdate;
  private boolean roleUpdated;

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
   * SUCCESS    - Ogel has been created on Spire, OgelSubmission updated with SpireRef, processing submission complete
   * FAILURE    - Ogel has not been created on Spire, terminal failure, processing submission complete
   */
  public enum Status {
    CREATED, CUSTOMER, SITE, USER_ROLE, READY, SUCCESS, FAILURE;
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

  public boolean isImmediate() {
    return mode.equals(OgelSubmission.Mode.IMMEDIATE);
  }

  public boolean hasCompleted() {
    return isSuccess() || isFailure();
  }

  public boolean isSuccess() {
    return status.equals(Status.SUCCESS);
  }

  public boolean isFailure() {
    return status.equals(Status.FAILURE);
  }

  public boolean isScheduled() {
    return mode.equals(OgelSubmission.Mode.SCHEDULED);
  }

  public boolean canCreateOgel() {
    return !needsCustomer() && !needsSite() && !needsRoleUpdate();
  }

  public void changeToScheduledMode() {
    mode = Mode.SCHEDULED;
  }

  public void updateStatusToSuccess() {
    status = Status.SUCCESS;
  }

  public String getFailedReason() {
    String reason = " Unable to create Ogel";
    if (needsCustomer()) {
      reason = " Unable to create Customer for Ogel";
    } else if (needsSite()) {
      reason = " Unable to create Site for Ogel";
    } else if (needsRoleUpdate()) {
      reason = " Unable to do role update for Ogel";
    }
    return reason;
  }

  /**
   * Sets appropriate Status value
   * - only if current STATUS is not READY or if OgelSubmission has not completed
   */
  public void updateStatus() {
    if (!status.equals(Status.READY) || !hasCompleted()) {
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

  public RegisterOgel getRegisterOgelFromJson() {
    RegisterOgel regOgel = null;
    ObjectMapper mapper = new ObjectMapper();
    try {
      regOgel = mapper.readValue(this.getJson(), RegisterOgel.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return regOgel;
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
}

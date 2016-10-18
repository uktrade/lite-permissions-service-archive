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
   * CREATED    - initial state on creation
   * CUSTOMER   - we need to create a Customer and populate customerId with resulting sarRef
   * SITE       - we need to create a Site and populate siteId with resulting siteRef
   * USER_ROLE  - we need to update user role permissions
   * READY      - this OgelSubmission is now setUp and we can create the Ogel via Spire
   * COMPLETE   - Ogel has been created on Spire, OgelSubmission updated with SpireRef, processing submission complete
   */
  public enum Status {
    CREATED, CUSTOMER, SITE, USER_ROLE, READY, COMPLETE;
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

  public boolean isScheduled() {
    return mode.equals(OgelSubmission.Mode.SCHEDULED);
  }

  public boolean canCreateOgel() {
    return !needsCustomer() && !needsSite() && !needsRoleUpdate();
  }

  public void changeToScheduledMode() {
    this.mode = Mode.SCHEDULED;
  }

  public void updateStatusToComplete() {
    this.status = Status.COMPLETE;
  }

  /**
   * Sets appropriate Status value (only if current STATUS is not READY or COMPLETE)
   */
  public void updateStatus() {
    if(!this.status.equals(Status.READY) || !this.status.equals(Status.COMPLETE)) {
      if (needsCustomer()) {
        this.status = Status.CUSTOMER;
      } else if (needsSite()) {
        this.status = Status.SITE;
      } else if (needsRoleUpdate()) {
        this.status = Status.USER_ROLE;
      } else {
        this.status = Status.READY;
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
}

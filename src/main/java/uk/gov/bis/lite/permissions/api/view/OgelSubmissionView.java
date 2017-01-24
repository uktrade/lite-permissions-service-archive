package uk.gov.bis.lite.permissions.api.view;


public class OgelSubmissionView {

  private String id;
  private String userId;
  private String ogelType;
  private String mode;
  private String status;
  private String submissionRef;
  private String customerRef;
  private String siteRef;
  private String spireRef;
  private String firstFail;
  private String lastFailMessage;
  private String failReason;
  private String callbackUrl;
  private boolean calledBack;
  private String created;
  private boolean roleUpdate;
  private boolean roleUpdated;

  public String getId() {
    return id;
  }

  public void setId(String id) {
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

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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

  public String getSpireRef() {
    return spireRef;
  }

  public void setSpireRef(String spireRef) {
    this.spireRef = spireRef;
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

  public String getFailReason() {
    return failReason;
  }

  public void setFailReason(String failReason) {
    this.failReason = failReason;
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

  public boolean isRoleUpdated() {
    return roleUpdated;
  }

  public void setRoleUpdated(boolean roleUpdated) {
    this.roleUpdated = roleUpdated;
  }
}
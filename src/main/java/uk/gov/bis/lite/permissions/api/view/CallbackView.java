package uk.gov.bis.lite.permissions.api.view;

public class CallbackView {

  private String requestId;
  private String status;
  private String registrationReference;
  private FailReason failReason;

  public enum FailReason {
    PERMISSION_DENIED, SITE_ALREADY_REGISTERED, BLACKLISTED, ENDPOINT_ERROR, UNCLASSIFIED;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getRegistrationReference() {
    return registrationReference;
  }

  public void setRegistrationReference(String registrationReference) {
    this.registrationReference = registrationReference;
  }

  public FailReason getFailReason() {
    return failReason;
  }

  public void setFailReason(FailReason failReason) {
    this.failReason = failReason;
  }
}

package uk.gov.bis.lite.permissions.api.view;

public class CallbackView {

  private String requestId;
  private Status status;
  private String registrationReference;
  private FailReason failReason;

  public enum FailReason {
    PERMISSION_DENIED, SITE_ALREADY_REGISTERED, BLACKLISTED, ENDPOINT_ERROR, UNCLASSIFIED;
  }

  public enum Status {
    SUCCESS, FAILED;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
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

package uk.gov.bis.lite.permissions.api.view;

public class CallbackView {

  private Result result;
  private String requestId;
  private String registrationReference;
  private String customerId;
  private String siteId;

  public enum Result {
    SUCCESS, PERMISSION_DENIED, SITE_ALREADY_REGISTERED, BLACKLISTED, FAILED;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public Result getResult() {
    return result;
  }

  public void setResult(Result result) {
    this.result = result;
  }

  public String getRegistrationReference() {
    return registrationReference;
  }

  public void setRegistrationReference(String registrationReference) {
    this.registrationReference = registrationReference;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getSiteId() {
    return siteId;
  }

  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }
}

package uk.gov.bis.lite.permissions.api.view;

public class OgelRegistrationView {

  private String ogelType;
  private String registrationReference;
  private String registrationDate;
  private String customerId;
  private String siteId;
  private Status status;

  public enum Status {
    EXTANT, DEREGISTERED, SURRENDERED, UNKNOWN
  }

  public OgelRegistrationView() {
  }

  public String getOgelType() {
    return ogelType;
  }

  public void setOgelType(String ogelType) {
    this.ogelType = ogelType;
  }

  public String getRegistrationReference() {
    return registrationReference;
  }

  public void setRegistrationReference(String registrationReference) {
    this.registrationReference = registrationReference;
  }

  public String getRegistrationDate() {
    return registrationDate;
  }

  public void setRegistrationDate(String registrationDate) {
    this.registrationDate = registrationDate;
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

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }
}

package uk.gov.bis.lite.permissions.spire.model;

public class SpireOgelRegistration {

  private String sarRef;
  private String ogelTypeRef;
  private String registrationRef;
  private String registrationDate;
  private String siteRef;
  private String status;


  public String getSarRef() {
    return sarRef;
  }

  public void setSarRef(String sarRef) {
    this.sarRef = sarRef;
  }

  public String getOgelTypeRef() {
    return ogelTypeRef;
  }

  public void setOgelTypeRef(String ogelTypeRef) {
    this.ogelTypeRef = ogelTypeRef;
  }

  public String getRegistrationRef() {
    return registrationRef;
  }

  public void setRegistrationRef(String registrationRef) {
    this.registrationRef = registrationRef;
  }

  public String getRegistrationDate() {
    return registrationDate;
  }

  public void setRegistrationDate(String registrationDate) {
    this.registrationDate = registrationDate;
  }

  public String getSiteRef() {
    return siteRef;
  }

  public void setSiteRef(String siteRef) {
    this.siteRef = siteRef;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}

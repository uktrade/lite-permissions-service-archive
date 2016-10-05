package uk.gov.bis.lite.permissions.model;

public class OgelReg {

  private String ogelTypeRef;
  private String sarRef;
  private String registrationRef;
  private String registrationDate;
  private String status;

  public String getOgelTypeRef() {
    return ogelTypeRef;
  }

  public void setOgelTypeRef(String ogelTypeRef) {
    this.ogelTypeRef = ogelTypeRef;
  }

  public String getSarRef() {
    return sarRef;
  }

  public void setSarRef(String sarRef) {
    this.sarRef = sarRef;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}

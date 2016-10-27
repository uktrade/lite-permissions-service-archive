package uk.gov.bis.lite.spire.model;

public class SpireSite {

  private String siteRef;
  private String sarRef;
  private String division;
  private String address;
  private String companyName;
  private String occupancyStatus;
  private String applicantType;

  public String getSiteRef() {
    return siteRef;
  }

  public void setSiteRef(String siteRef) {
    this.siteRef = siteRef;
  }

  public String getSarRef() {
    return sarRef;
  }

  public void setSarRef(String sarRef) {
    this.sarRef = sarRef;
  }

  public String getDivision() {
    return division;
  }

  public void setDivision(String division) {
    this.division = division;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getOccupancyStatus() {
    return occupancyStatus;
  }

  public void setOccupancyStatus(String occupancyStatus) {
    this.occupancyStatus = occupancyStatus;
  }

  public String getApplicantType() {
    return applicantType;
  }

  public void setApplicantType(String applicantType) {
    this.applicantType = applicantType;
  }
}

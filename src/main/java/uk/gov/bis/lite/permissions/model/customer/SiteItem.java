package uk.gov.bis.lite.permissions.model.customer;

public class SiteItem {

  private String userId;
  private String sarRef;
  private String division;
  private String liteAddress;
  private String address;
  private String countryRef;


  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
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

  public String getLiteAddress() {
    return liteAddress;
  }

  public void setLiteAddress(String liteAddress) {
    this.liteAddress = liteAddress;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getCountryRef() {
    return countryRef;
  }

  public void setCountryRef(String countryRef) {
    this.countryRef = countryRef;
  }
}

package uk.gov.bis.lite.permissions.model.customer;

public class CustomerItem {

  private String userId;
  private String customerName;
  private String customerType;
  private String liteAddress;
  private String address;
  private String countryRef;
  private String website;
  private String companiesHouseNumber;
  private Boolean companiesHouseValidated;
  private String eoriNumber;
  private Boolean eoriValidated;


  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getCustomerType() {
    return customerType;
  }

  public void setCustomerType(String customerType) {
    this.customerType = customerType;
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

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public String getCompaniesHouseNumber() {
    return companiesHouseNumber;
  }

  public void setCompaniesHouseNumber(String companiesHouseNumber) {
    this.companiesHouseNumber = companiesHouseNumber;
  }

  public Boolean getCompaniesHouseValidated() {
    return companiesHouseValidated;
  }

  public void setCompaniesHouseValidated(Boolean companiesHouseValidated) {
    this.companiesHouseValidated = companiesHouseValidated;
  }

  public String getEoriNumber() {
    return eoriNumber;
  }

  public void setEoriNumber(String eoriNumber) {
    this.eoriNumber = eoriNumber;
  }

  public Boolean getEoriValidated() {
    return eoriValidated;
  }

  public void setEoriValidated(Boolean eoriValidated) {
    this.eoriValidated = eoriValidated;
  }
}


package uk.gov.bis.lite.permissions.model.request;

import uk.gov.bis.lite.permissions.util.Util;

class Customer {

  private String customerType;
  private String chNumber;
  private String chNumberValidated;
  private String eoriNumber;
  private String eoriNumberValidated;
  private String website;
  private Address registeredAddress;

  public String getInfo() {
    String info = "\nCustomer " +
        Util.info("customerType", customerType) +
        Util.info("chNumber", chNumber) +
        Util.info("chNumberValidated", chNumberValidated) +
        Util.info("eoriNumber", eoriNumber) +
        Util.info("eoriNumberValidated", eoriNumberValidated) +
        Util.info("website", website);
    if (registeredAddress != null) {
      info = info + "\nCustomerRegistered" + registeredAddress.getInfo();
    }
    return info;
  }

  public Address getRegisteredAddress() {
    return registeredAddress;
  }

  public void setRegisteredAddress(Address registeredAddress) {
    this.registeredAddress = registeredAddress;
  }

  public String getChNumber() {
    return chNumber;
  }

  public void setChNumber(String chNumber) {
    this.chNumber = chNumber;
  }

  public String getChNumberValidated() {
    return chNumberValidated;
  }

  public void setChNumberValidated(String chNumberValidated) {
    this.chNumberValidated = chNumberValidated;
  }

  public String getEoriNumber() {
    return eoriNumber;
  }

  public void setEoriNumber(String eoriNumber) {
    this.eoriNumber = eoriNumber;
  }

  public String getEoriNumberValidated() {
    return eoriNumberValidated;
  }

  public void setEoriNumberValidated(String eoriNumberValidated) {
    this.eoriNumberValidated = eoriNumberValidated;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public String getCustomerType() {
    return customerType;
  }

  public void setCustomerType(String customerType) {
    this.customerType = customerType;
  }
}

package uk.gov.bis.lite.permissions.model.request;

import uk.gov.bis.lite.permissions.util.Util;

import java.util.Objects;

class Customer {

  private String customerType;
  private String chNumber;
  private boolean chNumberValidated;
  private String eoriNumber;
  private boolean eoriNumberValidated;
  private String website;
  private Address registeredAddress;

  String getJoinedInstanceStateData() {
    String strings = Util.joinAll(customerType, chNumber, eoriNumber, website);
    String booleans = Util.joinAll(chNumberValidated, eoriNumberValidated);
    String address = registeredAddress != null ? registeredAddress.getJoinedInstanceStateData() : "";
    return strings + booleans + address;
  }

  public String getInfo() {
    String info = "\nCustomer " +
        Util.info("customerType", customerType) +
        Util.info("chNumber", chNumber) +
        Util.info("chNumberValidated", chNumberValidated) +
        Util.info("eoriNumber", eoriNumber) +
        Util.info("eoriNumberValidated", eoriNumberValidated) +
        Util.info("website", website);
    return info + (registeredAddress != null ? registeredAddress.getInfo() : "");
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Customer) {
      Customer customer = (Customer) o;
      return Objects.equals(customerType, customer.getCustomerType())
          && Objects.equals(chNumber, customer.getChNumber())
          && Objects.equals(chNumberValidated, customer.isChNumberValidated())
          && Objects.equals(eoriNumber, customer.getEoriNumber())
          && Objects.equals(eoriNumberValidated, customer.isEoriNumberValidated())
          && Objects.equals(website, customer.getWebsite())
          && Objects.equals(registeredAddress, customer.getRegisteredAddress());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(customerType, chNumber, chNumberValidated, eoriNumber, eoriNumberValidated, website, registeredAddress);
  }

  /**
   * Getters/Setters
   */
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

  public String getEoriNumber() {
    return eoriNumber;
  }

  public void setEoriNumber(String eoriNumber) {
    this.eoriNumber = eoriNumber;
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

  public boolean isChNumberValidated() {
    return chNumberValidated;
  }

  public void setChNumberValidated(boolean chNumberValidated) {
    this.chNumberValidated = chNumberValidated;
  }

  public boolean isEoriNumberValidated() {
    return eoriNumberValidated;
  }

  public void setEoriNumberValidated(boolean eoriNumberValidated) {
    this.eoriNumberValidated = eoriNumberValidated;
  }
}

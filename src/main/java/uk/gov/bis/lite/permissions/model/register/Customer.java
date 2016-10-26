package uk.gov.bis.lite.permissions.model.register;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.permissions.util.Util;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer {

  private String customerName;
  private String customerType;
  private String chNumber;
  private boolean chNumberValidated;
  private String eoriNumber;
  private boolean eoriNumberValidated;
  private String website;
  private Address registeredAddress;

  String getJoinedInstanceStateData() {
    String strings = StringUtils.join(customerName, customerType, chNumber, eoriNumber, website);
    String booleans = StringUtils.join(chNumberValidated, eoriNumberValidated);
    String address = registeredAddress != null ? registeredAddress.getAddressData() : "";
    return strings + booleans + address;
  }

  public String getInfo() {
    String info = "\nCustomer " +
        Util.info("customerName", customerName) +
        Util.info("customerType", customerType) +
        Util.info("chNumber", chNumber) +
        Util.info("chNumberValidated", chNumberValidated) +
        Util.info("eoriNumber", eoriNumber) +
        Util.info("eoriNumberValidated", eoriNumberValidated) +
        Util.info("website", website);
    return info + (registeredAddress != null ? registeredAddress.getInfo() : "");
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

  public Boolean isChNumberValidated() {
    return chNumberValidated;
  }

  public void setChNumberValidated(boolean chNumberValidated) {
    this.chNumberValidated = chNumberValidated;
  }

  public Boolean isEoriNumberValidated() {
    return eoriNumberValidated;
  }

  public void setEoriNumberValidated(boolean eoriNumberValidated) {
    this.eoriNumberValidated = eoriNumberValidated;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }
}

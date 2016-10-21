package uk.gov.bis.lite.permissions.spire.model;

import uk.gov.bis.lite.permissions.model.register.Address;

public class AddressItem {

  private String line1;
  private String line2;
  private String town;
  private String county;
  private String postcode;
  private String country;

  public void init(Address address) {
    this.line1 = address.getLine1();
    this.line2 = address.getLine2();
    this.town = address.getTown();
    this.county = address.getCounty();
    this.postcode = address.getPostcode();
    this.country = address.getCountry();
  }

  public String getLine1() {
    return line1;
  }

  public void setLine1(String line1) {
    this.line1 = line1;
  }

  public String getLine2() {
    return line2;
  }

  public void setLine2(String line2) {
    this.line2 = line2;
  }

  public String getTown() {
    return town;
  }

  public void setTown(String town) {
    this.town = town;
  }

  public String getCounty() {
    return county;
  }

  public void setCounty(String county) {
    this.county = county;
  }

  public String getPostcode() {
    return postcode;
  }

  public void setPostcode(String postcode) {
    this.postcode = postcode;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

}
package uk.gov.bis.lite.permissions.model.register;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.bis.lite.permissions.util.Util;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

  private String line1;
  private String line2;
  private String town;
  private String county;
  private String postcode;
  private String country;

  public String getLiteAddress() {
    return Util.joinDelimited(", ", line1, line2, town, county, postcode, country);
  }

  public String getSpireAddress() {
    return Util.joinDelimited(", ", line1, line2, town, county, postcode, country);
  }

  public String getAddressData() {
    return Util.joinDelimited("", line1, line2, town, county, postcode, country);
  }

  public boolean isFullAddress() {
    return Util.allNotBlank(line1, line2, town, county, postcode, country);
  }

  public String getInfo() {
    return "Address " +
        Util.info("line1", line1) +
        Util.info("line2", line2) +
        Util.info("town", town) +
        Util.info("county", county) +
        Util.info("postcode", postcode) +
        Util.info("country", country);
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

package uk.gov.bis.lite.permissions.api.param;

import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.permissions.util.Util;

public class RegisterAddressParam {

  private String line1;
  private String line2;
  private String town;
  private String county;
  private String postcode;
  private String country;

  /**
   * At least one part of address must be not null to be valid
   */
  boolean valid() {
    boolean valid = false;
    if (!StringUtils.isBlank(line1) || !StringUtils.isBlank(line2) || !StringUtils.isBlank(town)
        || !StringUtils.isBlank(county) || !StringUtils.isBlank(postcode) || !StringUtils.isBlank(country)) {
      valid = true;
    }
    return valid;
  }

  String addressData() {
    return Util.joinDelimited("", line1, line2, town, county, postcode, country);
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

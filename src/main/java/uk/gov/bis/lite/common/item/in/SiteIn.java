package uk.gov.bis.lite.common.item.in;

import uk.gov.bis.lite.common.item.AddressItem;

public class SiteIn {

  private String siteName;
  private AddressItem address;

  public String getSiteName() {
    return siteName;
  }

  public void setSiteName(String siteName) {
    this.siteName = siteName;
  }

  public AddressItem getAddress() {
    return address;
  }

  public void setAddress(AddressItem address) {
    this.address = address;
  }
}

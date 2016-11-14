package uk.gov.bis.lite.common.item.out;

public class SiteOut {

  private String customerId;
  private String siteId;
  private String siteName;
  private AddressOut address;

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getSiteId() {
    return siteId;
  }

  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  public String getSiteName() {
    return siteName;
  }

  public void setSiteName(String siteName) {
    this.siteName = siteName;
  }

  public AddressOut getAddress() {
    return address;
  }

  public void setAddress(AddressOut address) {
    this.address = address;
  }
}

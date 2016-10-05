package uk.gov.bis.lite.permissions.model.request;

import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.permissions.util.Util;

class Site {

  private String useCustomerAddress;
  private String siteName;
  private Address address;

  public boolean isValid() {
    boolean valid = false;
    if(StringUtils.isBlank(useCustomerAddress)) {
      if(!StringUtils.isBlank(siteName) && address != null) {
        if(address.isFullAddress()) {
          valid = true;
        }
      }
    } else {
      if(StringUtils.isBlank(siteName) && address == null) {
        valid = true;
      }
    }
    return valid;
  }

  public String getInfo() {
    String info =  "\nSite " +
        Util.info("useCustomerAddress", useCustomerAddress)  +
        Util.info("siteName", siteName);

    if(address != null) {
      info = info + "\nSite" + address.getInfo();
    }
    return info;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public String getUseCustomerAddress() {
    return useCustomerAddress;
  }

  public void setUseCustomerAddress(String useCustomerAddress) {
    this.useCustomerAddress = useCustomerAddress;
  }

  public String getSiteName() {
    return siteName;
  }

  public void setSiteName(String siteName) {
    this.siteName = siteName;
  }
}

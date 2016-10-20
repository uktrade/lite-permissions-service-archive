package uk.gov.bis.lite.permissions.model.register;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.permissions.util.Util;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Site {

  private boolean useCustomerAddress;
  private String siteName;
  private Address address;

  /**
   * We pass in Customer so check can be done against Customer Address if required
   */
  boolean isValid(Customer customer) {
    boolean valid = false;
    if (useCustomerAddress) {
      if (customer != null) {
        Address customerAddress = customer.getRegisteredAddress();
        if (customerAddress != null) {
          if (customerAddress.isFullAddress()) {
            return true;
          }
        }
      }
    } else {
      valid = isValid(siteName, address);
    }
    return valid;
  }

  public String getInfo() {
    String info = "\nSite " +
        Util.info("useCustomerAddress", useCustomerAddress) +
        Util.info("siteName", siteName);
    return info + (address != null ? address.getInfo() : "");
  }

  String getJoinedInstanceStateData() {
    String strings = Util.joinAll(siteName);
    String booleans = Util.joinAll(useCustomerAddress);
    String add = address != null ? address.getAddressData() : "";
    return strings + booleans + add;
  }

  private boolean isValid(String siteName, Address address) {
    if (!StringUtils.isBlank(siteName) && address != null) {
      if (address.isFullAddress()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Getters/Setters
   */
  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public boolean isUseCustomerAddress() {
    return useCustomerAddress;
  }

  public void setUseCustomerAddress(boolean useCustomerAddress) {
    this.useCustomerAddress = useCustomerAddress;
  }

  public String getSiteName() {
    return siteName;
  }

  public void setSiteName(String siteName) {
    this.siteName = siteName;
  }
}

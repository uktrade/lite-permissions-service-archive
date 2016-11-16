package uk.gov.bis.lite.permissions.api.param;

import org.apache.commons.lang3.StringUtils;

/**
 * RegisterParam
 *
 * Including static nested classes:
 *  RegisterSiteParam
 *  RegisterCustomerParam
 *  RegisterAdminApprovalParam
 */
public class RegisterParam {

  private String userId;
  private String ogelType;
  private String existingCustomer;
  private String existingSite;

  private RegisterSiteParam newSite;
  private RegisterCustomerParam newCustomer;
  private RegisterAdminApprovalParam adminApproval;

  public boolean roleUpdateRequired() {
    return adminApproval != null && !StringUtils.isBlank(adminApproval.getAdminUserId());
  }

  public boolean valid() {
    boolean valid = mandatoryFieldsOk() && customerFieldsOk() && siteFieldsOk();
    if (valid && newSite != null) {
      valid = newSite.valid(newCustomer);
    }
    return valid;
  }

  public String validationInfo() {
    String info = !mandatoryFieldsOk() ? "Fields are mandatory: userId, ogelType. " : "";
    String customerCheck = !customerFieldsOk() ? "Must have existing Customer or new Customer fields. " : "";
    String siteCheck = !siteFieldsOk() ? "Must have existing Site or new Site fields. " : "";
    info = info + customerCheck + siteCheck;
    if (newSite != null) {
      if (!newSite.valid(newCustomer)) {
        info = info + "New Site must have full address";
      }
    }
    return info;
  }

  /**
   * RegisterSiteParam
   */
  public static class RegisterSiteParam {
    private boolean useCustomerAddress;
    private String siteName;
    private RegisterAddressParam address;

    boolean valid(RegisterCustomerParam customer) {
      boolean valid = false;
      if (useCustomerAddress) {
        if (customer != null) {
          RegisterAddressParam address = customer.getRegisteredAddress();
          if (address != null) {
            if (address.valid()) {
              return true;
            }
          }
        }
      } else {
        valid = valid(siteName, address);
      }
      return valid;
    }

    String joinedInstanceStateData() {
      String strings = StringUtils.join(siteName);
      String booleans = StringUtils.join(useCustomerAddress);
      String add = address != null ? address.addressData() : "";
      return strings + booleans + add;
    }

    private boolean valid(String siteName, RegisterAddressParam address) {
      if (!StringUtils.isBlank(siteName) && address != null) {
        if (address.valid()) {
          return true;
        }
      }
      return false;
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

    public RegisterAddressParam getAddress() {
      return address;
    }

    public void setAddress(RegisterAddressParam address) {
      this.address = address;
    }
  }

  /**
   * RegisterCustomerParam
   */
  public static class RegisterCustomerParam {
    private String customerName;
    private String customerType;
    private String chNumber;
    private boolean chNumberValidated;
    private String eoriNumber;
    private boolean eoriNumberValidated;
    private String website;
    private RegisterAddressParam registeredAddress;

    String joinedInstanceStateData() {
      String strings = StringUtils.join(customerName, customerType, chNumber, eoriNumber, website);
      String booleans = StringUtils.join(chNumberValidated, eoriNumberValidated);
      String address = registeredAddress != null ? registeredAddress.addressData() : "";
      return strings + booleans + address;
    }

    public String getCustomerName() {
      return customerName;
    }

    public void setCustomerName(String customerName) {
      this.customerName = customerName;
    }

    public String getCustomerType() {
      return customerType;
    }

    public void setCustomerType(String customerType) {
      this.customerType = customerType;
    }

    public String getChNumber() {
      return chNumber;
    }

    public void setChNumber(String chNumber) {
      this.chNumber = chNumber;
    }

    public boolean isChNumberValidated() {
      return chNumberValidated;
    }

    public void setChNumberValidated(boolean chNumberValidated) {
      this.chNumberValidated = chNumberValidated;
    }

    public String getEoriNumber() {
      return eoriNumber;
    }

    public void setEoriNumber(String eoriNumber) {
      this.eoriNumber = eoriNumber;
    }

    public boolean isEoriNumberValidated() {
      return eoriNumberValidated;
    }

    public void setEoriNumberValidated(boolean eoriNumberValidated) {
      this.eoriNumberValidated = eoriNumberValidated;
    }

    public String getWebsite() {
      return website;
    }

    public void setWebsite(String website) {
      this.website = website;
    }

    public RegisterAddressParam getRegisteredAddress() {
      return registeredAddress;
    }

    public void setRegisteredAddress(RegisterAddressParam registeredAddress) {
      this.registeredAddress = registeredAddress;
    }
  }

  /**
   * RegisterAdminApprovalParam
   */
  public static class RegisterAdminApprovalParam {
    private String adminUserId;

    String joinedInstanceStateData() {
      return adminUserId != null ? adminUserId : "";
    }

    public String getAdminUserId() {
      return adminUserId;
    }

    public void setAdminUserId(String adminUserId) {
      this.adminUserId = adminUserId;
    }
  }

  /**
   * Private methods
   */
  public String joinedInstanceStateData() {
    String strings = StringUtils.join(userId, ogelType, existingCustomer, existingSite);
    String customer = newCustomer != null ? newCustomer.joinedInstanceStateData() : "";
    String site = newSite != null ? newSite.joinedInstanceStateData() : "";
    String admin = adminApproval != null ? adminApproval.joinedInstanceStateData() : "";
    return strings + customer + site + admin;
  }

  private boolean mandatoryFieldsOk() {
    return !StringUtils.isBlank(userId) && !StringUtils.isBlank(ogelType);
  }

  private boolean customerFieldsOk() {
    return (StringUtils.isBlank(existingCustomer) && newCustomer != null) ||
        (!StringUtils.isBlank(existingCustomer) && newCustomer == null);
  }

  private boolean siteFieldsOk() {
    return (StringUtils.isBlank(existingSite) && newSite != null) ||
        (!StringUtils.isBlank(existingSite) && newSite == null);
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getOgelType() {
    return ogelType;
  }

  public void setOgelType(String ogelType) {
    this.ogelType = ogelType;
  }

  public String getExistingCustomer() {
    return existingCustomer;
  }

  public void setExistingCustomer(String existingCustomer) {
    this.existingCustomer = existingCustomer;
  }

  public String getExistingSite() {
    return existingSite;
  }

  public void setExistingSite(String existingSite) {
    this.existingSite = existingSite;
  }

  public RegisterSiteParam getNewSite() {
    return newSite;
  }

  public void setNewSite(RegisterSiteParam newSite) {
    this.newSite = newSite;
  }

  public RegisterCustomerParam getNewCustomer() {
    return newCustomer;
  }

  public void setNewCustomer(RegisterCustomerParam newCustomer) {
    this.newCustomer = newCustomer;
  }

  public RegisterAdminApprovalParam getAdminApproval() {
    return adminApproval;
  }

  public void setAdminApproval(RegisterAdminApprovalParam adminApproval) {
    this.adminApproval = adminApproval;
  }
}

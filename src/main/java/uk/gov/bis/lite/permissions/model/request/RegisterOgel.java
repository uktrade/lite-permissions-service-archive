package uk.gov.bis.lite.permissions.model.request;

import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.permissions.util.Util;

public class RegisterOgel {

  private String userId;
  private String ogelType;
  private String existingCustomer;
  private String existingSite;
  private Customer newCustomer;
  private Site newSite;
  private AdminApproval adminApproval;

  public boolean isValid() {
    boolean valid = mandatoryFieldsOk() && customerFieldsOk() && siteFieldsOk();
    if (newSite != null) {
      valid = newSite.isValid();
    }
    return valid;
  }

  public String getValidityInfo() {
    String info = Util.getOptString("Fields are mandatory: userId, ogelType. ", !mandatoryFieldsOk());
    info = info + Util.getOptString("Must have existing Customer or new Customer fields. ", !customerFieldsOk());
    info = info + Util.getOptString("Must have existing Site or new Site fields. ", !siteFieldsOk());
    if (newSite != null) {
      if (!newSite.isValid()) {
        info = info + "New Site must have full address";
      }
    }
    return info;
  }

  @Override
  public String toString() {
    String info = "\nRegisterOgel " + Util.info("userId", userId) + Util.info("ogelType", ogelType);
    if (newCustomer != null) {
      info = info + newCustomer.getInfo();
    }
    if (adminApproval != null) {
      info = info + adminApproval.getInfo();
    }
    if (newSite != null) {
      info = info + newSite.getInfo();
    }
    return info;
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

  public AdminApproval getAdminApproval() {
    return adminApproval;
  }

  public void setAdminApproval(AdminApproval adminApproval) {
    this.adminApproval = adminApproval;
  }

  public Site getNewSite() {
    return newSite;
  }

  public void setNewSite(Site newSite) {
    this.newSite = newSite;
  }

  public Customer getNewCustomer() {
    return newCustomer;
  }

  public void setNewCustomer(Customer newCustomer) {
    this.newCustomer = newCustomer;
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
}

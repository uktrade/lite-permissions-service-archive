package uk.gov.bis.lite.permissions.model.register;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.util.Util;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterOgel {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterOgel.class);

  private String userId;
  private String ogelType;
  private String existingCustomer;
  private String existingSite;
  private Customer newCustomer;
  private Site newSite;
  private AdminApproval adminApproval;

  private transient String responseMessage;

  public boolean isExistingCustomerAndSite() {
    return !StringUtils.isBlank(existingCustomer) && !StringUtils.isBlank(existingSite);
  }

  public boolean isExistingCustomer() {
    return !StringUtils.isBlank(existingCustomer);
  }

  public boolean isExistingSite() {
    return !StringUtils.isBlank(existingSite);
  }

  public boolean isRoleUpdateRequired() {
    if (adminApproval != null && !StringUtils.isBlank(adminApproval.getAdminUserId())) {
      return true;
    }
    return false;
  }

  public boolean isValid() {
    boolean valid = mandatoryFieldsOk() && customerFieldsOk() && siteFieldsOk();
    if (valid && newSite != null) {
      valid = newSite.isValid(newCustomer);
    }
    if (!valid) {
      responseMessage = getValidityInfo();
    }
    return valid;
  }

  public String getValidityInfo() {
    String info = !mandatoryFieldsOk() ? "Fields are mandatory: userId, ogelType. " : "";
    String customerCheck = !customerFieldsOk() ? "Must have existing Customer or new Customer fields. " : "";
    String siteCheck = !siteFieldsOk() ? "Must have existing Site or new Site fields. " : "";
    info = info + customerCheck + siteCheck;
    if (newSite != null) {
      if (!newSite.isValid(newCustomer)) {
        info = info + "New Site must have full address";
      }
    }
    return info;
  }

  /**
   * Gathers data, creates  hash
   */
  public String generateSubmissionReference() {
    String message = getJoinedInstanceStateData().replaceAll("\\s+", "").toUpperCase();
    return Util.generateHashFromString(message);
  }

  @Override
  public String toString() {
    String info = "\nRegisterOgel " + Util.info("userId", userId) + Util.info("ogelType", ogelType);
    info = info + (newCustomer != null ? newCustomer.getInfo() : "");
    info = info + (adminApproval != null ? adminApproval.getInfo() : "");
    return info + (newSite != null ? newSite.getInfo() : "");
  }

  /**
   * Private methods
   */
  private String getJoinedInstanceStateData() {
    String strings = StringUtils.join(userId, ogelType, existingCustomer, existingSite);
    String customer = newCustomer != null ? newCustomer.getJoinedInstanceStateData() : "";
    String site = newSite != null ? newSite.getJoinedInstanceStateData() : "";
    String admin = adminApproval != null ? adminApproval.getJoinedInstanceStateData() : "";
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

  /**
   * Getters/Setters
   */
  public String getResponseMessage() {
    return responseMessage;
  }

  public void setResponseMessage(String responseMessage) {
    this.responseMessage = responseMessage;
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
}
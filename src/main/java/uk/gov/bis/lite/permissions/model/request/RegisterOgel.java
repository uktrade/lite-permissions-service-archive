package uk.gov.bis.lite.permissions.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.util.Util;

import java.util.Objects;

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
    String info = Util.getOptString("Fields are mandatory: userId, ogelType. ", !mandatoryFieldsOk());
    info = info + Util.getOptString("Must have existing Customer or new Customer fields. ", !customerFieldsOk());
    info = info + Util.getOptString("Must have existing Site or new Site fields. ", !siteFieldsOk());
    if (newSite != null) {
      if (!newSite.isValid(newCustomer)) {
        info = info + "New Site must have full address";
      }
    }
    return info;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof RegisterOgel) {
      RegisterOgel regOgel = (RegisterOgel) o;
      return Objects.equals(userId, regOgel.getUserId())
          && Objects.equals(ogelType, regOgel.getOgelType())
          && Objects.equals(existingCustomer, regOgel.getExistingCustomer())
          && Objects.equals(existingSite, regOgel.getExistingSite())
          && Objects.equals(newCustomer, regOgel.getNewCustomer())
          && Objects.equals(newSite, regOgel.getNewSite())
          && Objects.equals(adminApproval, regOgel.getAdminApproval());
    }
    return false;
  }

  /**
   * Gathers data, creates  hash
   */
  public String getHashIdentifier() {
    String message = getJoinedInstanceStateData().replaceAll("\\s+", "").toUpperCase();
    return Util.generateHashFromString(message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, ogelType, existingCustomer, existingSite, newCustomer, newSite, adminApproval);
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
    String strings = Util.joinAll(userId, ogelType, existingCustomer, existingSite);
    String customer = newCustomer != null ? newCustomer.getJoinedInstanceStateData() : "";
    String site = newSite != null ? newSite.getJoinedInstanceStateData() : "";
    String admin = adminApproval != null ? adminApproval.getJoinedInstanceStateData() : "";
    //LOGGER.info(strings + customer + site + admin);
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
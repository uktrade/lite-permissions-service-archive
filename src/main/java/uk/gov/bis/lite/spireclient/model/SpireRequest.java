package uk.gov.bis.lite.spireclient.model;


import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.spireclient.SpireClientService;
import uk.gov.bis.lite.spireclient.items.AddressItem;

public class SpireRequest {

  private SpireClientService.Endpoint endpoint;
  private String userId;
  private String adminUserId;
  private String sarRef;
  private String siteRef;
  private String ogelType;
  private String roleType;
  private String siteName;
  private String customerName;
  private String customerType;
  private String companiesHouseNumber;
  private boolean companiesHouseValidated;
  private String eoriNumber;
  private boolean eoriValidated;
  private String website;

  private SpireAddress address;

  public SpireClientService.Endpoint getEndpoint() {
    return endpoint;
  }

  /**
   * At least one part of address must be not null to be valid
   */
  public boolean isAddressValid() {
    boolean valid = false;
    if(!StringUtils.isBlank(address.getLine1()) || !StringUtils.isBlank(address.getLine2()) || !StringUtils.isBlank(address.getTown())
        || !StringUtils.isBlank(address.getCounty()) || !StringUtils.isBlank(address.getPostcode()) || !StringUtils.isBlank(address.getCounty())) {
      valid = true;
    }
    return valid;
  }

  public String getEoriValidatedStr() {
    return eoriValidated ? "true" : "false";
  }

  public String getCompaniesHouseValidatedStr() {
    return companiesHouseValidated ? "true" : "false";
  }

  public void setAddressData(AddressItem item) {
    this.address = new SpireAddress();
    this.address.setLine1(item.getLine1());
    this.address.setLine2(item.getLine2());
    this.address.setTown(item.getTown());
    this.address.setCounty(item.getCounty());
    this.address.setPostcode(item.getPostcode());
    this.address.setCountry(item.getCountry());
  }


  public void setEndpoint(SpireClientService.Endpoint endpoint) {
    this.endpoint = endpoint;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getAdminUserId() {
    return adminUserId;
  }

  public void setAdminUserId(String adminUserId) {
    this.adminUserId = adminUserId;
  }

  public String getSarRef() {
    return sarRef;
  }

  public void setSarRef(String sarRef) {
    this.sarRef = sarRef;
  }

  public String getSiteRef() {
    return siteRef;
  }

  public void setSiteRef(String siteRef) {
    this.siteRef = siteRef;
  }

  public String getOgelType() {
    return ogelType;
  }

  public void setOgelType(String ogelType) {
    this.ogelType = ogelType;
  }

  public String getRoleType() {
    return roleType;
  }

  public void setRoleType(String roleType) {
    this.roleType = roleType;
  }

  public String getSiteName() {
    return siteName;
  }

  public void setSiteName(String siteName) {
    this.siteName = siteName;
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

  public String getCompaniesHouseNumber() {
    return companiesHouseNumber;
  }

  public void setCompaniesHouseNumber(String companiesHouseNumber) {
    this.companiesHouseNumber = companiesHouseNumber;
  }

  public boolean getCompaniesHouseValidated() {
    return companiesHouseValidated;
  }

  public void setCompaniesHouseValidated(boolean companiesHouseValidated) {
    this.companiesHouseValidated = companiesHouseValidated;
  }

  public String getEoriNumber() {
    return eoriNumber;
  }

  public void setEoriNumber(String eoriNumber) {
    this.eoriNumber = eoriNumber;
  }

  public boolean getEoriValidated() {
    return eoriValidated;
  }

  public void setEoriValidated(boolean eoriValidated) {
    this.eoriValidated = eoriValidated;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public SpireAddress getAddress() {
    return address;
  }

  public void setAddress(SpireAddress address) {
    this.address = address;
  }
}

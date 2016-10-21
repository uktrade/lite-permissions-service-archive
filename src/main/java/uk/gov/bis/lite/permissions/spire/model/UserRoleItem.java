package uk.gov.bis.lite.permissions.spire.model;

public class UserRoleItem {

  private String userId;
  private String adminUserId;
  private String siteRef;
  private String roleType;

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

  public String getSiteRef() {
    return siteRef;
  }

  public void setSiteRef(String siteRef) {
    this.siteRef = siteRef;
  }

  public String getRoleType() {
    return roleType;
  }

  public void setRoleType(String roleType) {
    this.roleType = roleType;
  }
}

package uk.gov.bis.lite.permissions.model.customer;

public class UserRoleItem {

  private String adminUserId;
  private String roleType;

  public String getAdminUserId() {
    return adminUserId;
  }

  public void setAdminUserId(String adminUserId) {
    this.adminUserId = adminUserId;
  }

  public String getRoleType() {
    return roleType;
  }

  public void setRoleType(String roleType) {
    this.roleType = roleType;
  }
}

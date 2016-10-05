package uk.gov.bis.lite.permissions.model.request;

import uk.gov.bis.lite.permissions.util.Util;

class AdminApproval {
  private String adminUserId;

  public String getInfo() {
    return "\nAdminApproval " + Util.info("adminUserId", adminUserId);
  }

  public String getAdminUserId() {
    return adminUserId;
  }

  public void setAdminUserId(String adminUserId) {
    this.adminUserId = adminUserId;
  }
}

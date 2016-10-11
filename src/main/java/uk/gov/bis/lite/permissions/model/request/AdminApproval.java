package uk.gov.bis.lite.permissions.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.bis.lite.permissions.util.Util;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminApproval {

  private String adminUserId;

  @Override
  public boolean equals(Object o) {
    if (o instanceof AdminApproval) {
      AdminApproval admin = (AdminApproval) o;
      return Objects.equals(adminUserId, admin.getAdminUserId());
    }
    return false;
  }

  String getJoinedInstanceStateData() {
    return adminUserId != null ? adminUserId : "";
  }

  @Override
  public int hashCode() {
    return Objects.hash(adminUserId);
  }

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

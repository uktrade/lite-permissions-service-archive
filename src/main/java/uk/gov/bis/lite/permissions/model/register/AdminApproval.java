package uk.gov.bis.lite.permissions.model.register;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.bis.lite.permissions.util.Util;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminApproval {

  private String adminUserId;

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

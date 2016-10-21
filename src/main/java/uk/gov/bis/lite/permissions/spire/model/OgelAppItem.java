package uk.gov.bis.lite.permissions.spire.model;

public class OgelAppItem {

  private String userId;
  private String sarRef;
  private String siteRef;
  private String ogelType;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
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
}

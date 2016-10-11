package uk.gov.bis.lite.permissions.model.customer;

public class CustomerResponse {

  private String sarRef;
  private String siteRef;


  public String getSiteRef() {
    return siteRef;
  }

  public void setSiteRef(String siteRef) {
    this.siteRef = siteRef;
  }

  public String getSarRef() {
    return sarRef;
  }

  public void setSarRef(String sarRef) {
    this.sarRef = sarRef;
  }
}

package uk.gov.bis.lite.permissions.model;

import uk.gov.bis.lite.permissions.spire.model.SpireOgelRegistration;

public class OgelRegistration {

  private SpireOgelRegistration registration;

  public OgelRegistration(SpireOgelRegistration registration) {
    this.registration = registration;
  }

  public String getOgelType() {
    return registration.getOgelTypeRef();
  }

  public String getRegistrationReference() {
    return registration.getRegistrationRef();
  }

  public String getRegistrationDate() {
    return registration.getRegistrationDate();
  }

  public String getCustomerId() {
    return registration.getSarRef();
  }

  public String getSiteId() {
    return registration.getSiteRef();
  }

  public String getStatus() {
    return registration.getStatus();
  }
}

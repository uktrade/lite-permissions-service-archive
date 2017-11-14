package uk.gov.bis.lite.permissions.service.model.registration;

import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.model.Status;

import java.util.Optional;

public class SingleRegistrationResult {
  private final Status status;
  private final Optional<OgelRegistrationView> registrationView;

  public SingleRegistrationResult(Status status, OgelRegistrationView registrationView) {
    this.status = status;
    this.registrationView = Optional.ofNullable(registrationView);
  }

  public Status getStatus() {
    return status;
  }

  public Optional<OgelRegistrationView> getRegistrationView() {
    return registrationView;
  }
}

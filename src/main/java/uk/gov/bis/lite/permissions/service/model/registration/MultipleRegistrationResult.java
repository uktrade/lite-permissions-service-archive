package uk.gov.bis.lite.permissions.service.model.registration;

import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.model.Status;

import java.util.Collections;
import java.util.List;

public class MultipleRegistrationResult {
  private final Status status;
  private final  List<OgelRegistrationView> registrationViews;

  public MultipleRegistrationResult(Status status, List<OgelRegistrationView> registrationViews) {
    this.status = status;
    if (registrationViews == null) {
      this.registrationViews = Collections.emptyList();
    } else {
      this.registrationViews = registrationViews;
    }
  }

  public Status getStatus() {
    return status;
  }

  public List<OgelRegistrationView> getRegistrationViews() {
    return registrationViews;
  }
}

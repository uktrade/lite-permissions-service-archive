package uk.gov.bis.lite.permissions.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

import java.util.List;

public class RegistrationResult {

  private final Status status;
  private final String errorMessage;
  private final List<OgelRegistrationView> ogelRegistrationViews;

  public RegistrationResult(@JsonProperty("status") Status status,
                            @JsonProperty("errorMessage") String errorMessage,
                            @JsonProperty("ogelRegistrationViews") List<OgelRegistrationView> ogelRegistrationViews) {
    this.status = status;
    this.errorMessage = errorMessage;
    this.ogelRegistrationViews = ogelRegistrationViews;
  }

  public Status getStatus() {
    return status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public List<OgelRegistrationView> getOgelRegistrationViews() {
    return ogelRegistrationViews;
  }

}

package uk.gov.bis.lite.permissions.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.bis.lite.permissions.api.view.LicenceView;

import java.util.List;

public class LicenceResult {

  private final Status status;
  private final String errorMessage;
  private final List<LicenceView> licenceViews;

  public LicenceResult(@JsonProperty("status") Status status,
                       @JsonProperty("errorMessage") String errorMessage,
                       @JsonProperty("licenceViews") List<LicenceView> licenceViews) {
    this.status = status;
    this.errorMessage = errorMessage;
    this.licenceViews = licenceViews;
  }

  public Status getStatus() {
    return status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public List<LicenceView> getLicenceViews() {
    return licenceViews;
  }

}

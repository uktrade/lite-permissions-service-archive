package uk.gov.bis.lite.permissions.service.model;

import uk.gov.bis.lite.permissions.api.view.LicenceView;

import java.util.List;

public class LicenceResult {

  private final Status status;
  private final String errorMessage;
  private final List<LicenceView> licenceViews;

  public LicenceResult(Status status, String errorMessage, List<LicenceView> licenceViews) {
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

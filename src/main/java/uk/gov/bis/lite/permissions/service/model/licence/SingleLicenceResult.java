package uk.gov.bis.lite.permissions.service.model.licence;

import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.service.model.Status;

import java.util.Optional;

public class SingleLicenceResult {
  private final Status status;
  private final Optional<LicenceView> licenceView;

  public SingleLicenceResult(Status status, LicenceView licenceView) {
    this.status = status;
    this.licenceView = Optional.ofNullable(licenceView);
  }

  public Status getStatus() {
    return status;
  }

  public Optional<LicenceView> getLicenceView() {
    return licenceView;
  }
}

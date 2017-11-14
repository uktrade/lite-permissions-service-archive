package uk.gov.bis.lite.permissions.service.model.licence;

import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.service.model.Status;

import java.util.Collections;
import java.util.List;

public class MultipleLicenceResult {
  private final Status status;
  private final List<LicenceView> licenceViews;

  public MultipleLicenceResult(Status status, List<LicenceView> licenceViews) {
    this.status = status;
    if (licenceViews == null) {
      this.licenceViews = Collections.emptyList();
    } else {
      this.licenceViews = licenceViews;
    }
  }

  public Status getStatus() {
    return status;
  }

  public List<LicenceView> getLicenceViews() {
    return licenceViews;
  }
}

package uk.gov.bis.lite.permissions.service.model;

import uk.gov.bis.lite.permissions.api.view.LicenceView;

import java.util.Optional;

public class SingleLicenceResult extends LicenceServiceResult<Optional<LicenceView>> {
  SingleLicenceResult(Status status, Optional<LicenceView> result) {
    super(status, result);
  }

  public static SingleLicenceResult ok(LicenceView result) {
    return new SingleLicenceResult(Status.OK, Optional.ofNullable(result));
  }

  public static SingleLicenceResult empty() {
    return ok(null);
  }

  public static SingleLicenceResult userIdNotFound() {
    return new SingleLicenceResult(Status.USER_ID_NOT_FOUND, null);
  }

}

package uk.gov.bis.lite.permissions.service.model.registration;

import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

import java.util.Optional;

public class SingleRegistrationResult extends RegistrationServiceResult<Optional<OgelRegistrationView>> {
  SingleRegistrationResult(Status status, Optional<OgelRegistrationView> result) {
    super(status, result);
  }

  public static SingleRegistrationResult ok(OgelRegistrationView result) {
    return new SingleRegistrationResult(RegistrationServiceResult.Status.OK, Optional.ofNullable(result));
  }

  public static SingleRegistrationResult empty() {
    return ok(null);
  }

  public static SingleRegistrationResult userIdNotFound() {
    return new SingleRegistrationResult(RegistrationServiceResult.Status.USER_ID_NOT_FOUND, null);
  }
}

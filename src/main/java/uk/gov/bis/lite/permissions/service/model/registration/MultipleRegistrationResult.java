package uk.gov.bis.lite.permissions.service.model.registration;

import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

import java.util.List;

public class MultipleRegistrationResult extends RegistrationServiceResult<List<OgelRegistrationView>> {
  MultipleRegistrationResult(Status status, List<OgelRegistrationView> result) {
    super(status, result);
  }

  public static MultipleRegistrationResult ok(List<OgelRegistrationView> result) {
    return new MultipleRegistrationResult(Status.OK, result);
  }

  public static MultipleRegistrationResult userIdNotFound() {
    return new MultipleRegistrationResult(Status.USER_ID_NOT_FOUND, null);
  }

}

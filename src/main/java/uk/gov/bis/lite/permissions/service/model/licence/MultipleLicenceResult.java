package uk.gov.bis.lite.permissions.service.model.licence;

import uk.gov.bis.lite.permissions.api.view.LicenceView;

import java.util.List;

public class MultipleLicenceResult extends LicenceServiceResult<List<LicenceView>> {
  MultipleLicenceResult(Status status, List<LicenceView> result) {
    super(status, result);
  }

  public static MultipleLicenceResult ok(List<LicenceView> result) {
    return new MultipleLicenceResult(Status.OK, result);
  }

  public static MultipleLicenceResult userIdNotFound() {
    return new MultipleLicenceResult(Status.USER_ID_NOT_FOUND, null);
  }

}

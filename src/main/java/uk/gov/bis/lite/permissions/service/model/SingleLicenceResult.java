package uk.gov.bis.lite.permissions.service.model;

import uk.gov.bis.lite.permissions.api.view.LicenceView;

public class SingleLicenceResult extends LicenceServiceResult<LicenceView> {
  SingleLicenceResult(Status status, LicenceView result) {
    super(status, result);
  }

  public static SingleLicenceResult ok(LicenceView result) {
    return new SingleLicenceResult(Status.OK, result);
  }

  public static SingleLicenceResult empty() {
    return new SingleLicenceResult(Status.OK, null);
  }

  public static SingleLicenceResult userIdNotFound() {
    return new SingleLicenceResult(Status.USER_ID_NOT_FOUND, null);
  }

  /**
   * Depending on the result of {@link SingleLicenceResult#getStatus()} will return:
   * <ul>
   *   <li>{@link SingleLicenceResult.Status#OK} - null or LicenceView</li>
   *   <li>{@link SingleLicenceResult.Status#USER_ID_NOT_FOUND} - null</li>
   * </ul>
   * @return the result of a call to the {@link uk.gov.bis.lite.permissions.service.LicenceService}
   */
  @Override
  public LicenceView getResult() {
    return super.getResult();
  }
}

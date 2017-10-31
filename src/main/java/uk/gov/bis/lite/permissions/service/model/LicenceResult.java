package uk.gov.bis.lite.permissions.service.model;

import uk.gov.bis.lite.permissions.api.view.LicenceView;

public class LicenceResult extends LicenceServiceResult<LicenceView> {
  LicenceResult(Status status, LicenceView result) {
    super(status, result);
  }

  public static LicenceResult ok(LicenceView result) {
    return new LicenceResult(Status.OK, result);
  }

  public static LicenceResult empty() {
    return new LicenceResult(Status.OK, null);
  }

  public static LicenceResult userIdNotFound() {
    return new LicenceResult(Status.USER_ID_NOT_FOUND, null);
  }

  /**
   * Depending on the result of {@link LicenceResult#getStatus()} will return:
   * <ul>
   *   <li>{@link LicenceResult.Status#OK} - null or LicenceView</li>
   *   <li>{@link LicenceResult.Status#USER_ID_NOT_FOUND} - null</li>
   * </ul>
   * @return the result of a call to the {@link uk.gov.bis.lite.permissions.service.LicenceService}
   */
  @Override
  public LicenceView getResult() {
    return super.getResult();
  }
}

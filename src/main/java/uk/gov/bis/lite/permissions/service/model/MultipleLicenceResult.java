package uk.gov.bis.lite.permissions.service.model;

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

  /**
   * Depending on the result of {@link MultipleLicenceResult#getStatus()} will return:
   * <ul>
   *   <li>{@link MultipleLicenceResult.Status#OK} - List&lt;LicenceView&gt;</li>
   *   <li>{@link MultipleLicenceResult.Status#USER_ID_NOT_FOUND} - null</li>
   * </ul>
   * @return the result of a call to the {@link uk.gov.bis.lite.permissions.service.LicenceService}
   */
  @Override
  public List<LicenceView> getResult() {
    return super.getResult();
  }
}

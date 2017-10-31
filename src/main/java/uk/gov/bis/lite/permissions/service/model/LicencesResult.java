package uk.gov.bis.lite.permissions.service.model;

import uk.gov.bis.lite.permissions.api.view.LicenceView;

import java.util.List;

public class LicencesResult extends LicenceServiceResult<List<LicenceView>> {
  LicencesResult(Status status, List<LicenceView> result) {
    super(status, result);
  }

  public static LicencesResult ok(List<LicenceView> result) {
    return new LicencesResult(Status.OK, result);
  }

  public static LicencesResult userIdNotFound() {
    return new LicencesResult(Status.USER_ID_NOT_FOUND, null);
  }

  /**
   * Depending on the result of {@link LicencesResult#getStatus()} will return:
   * <ul>
   *   <li>{@link LicencesResult.Status#OK} - List&lt;LicenceView&gt;</li>
   *   <li>{@link LicencesResult.Status#USER_ID_NOT_FOUND} - null</li>
   * </ul>
   * @return the result of a call to the {@link uk.gov.bis.lite.permissions.service.LicenceService}
   */
  @Override
  public List<LicenceView> getResult() {
    return super.getResult();
  }
}

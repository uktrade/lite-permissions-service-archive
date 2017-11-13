package uk.gov.bis.lite.permissions.service.model.licence;

import uk.gov.bis.lite.permissions.service.model.ServiceResult;

/**
 * Wrapper for {@link uk.gov.bis.lite.permissions.service.LicenceService} service calls
 * @param <T> Type to wrap
 */
public abstract class LicenceServiceResult<T> extends ServiceResult<T> {
  LicenceServiceResult(Status status, T result) {
    super(status, result);
  }
}

package uk.gov.bis.lite.permissions.service.model.registration;

import uk.gov.bis.lite.permissions.service.RegistrationService;
import uk.gov.bis.lite.permissions.service.model.ServiceResult;

/**
 * Wrapper for {@link RegistrationService} service calls
 * @param <T> Type to wrap
 */
public abstract class RegistrationServiceResult<T> extends ServiceResult<T> {
  RegistrationServiceResult(Status status, T result) {
    super(status, result);
  }
}
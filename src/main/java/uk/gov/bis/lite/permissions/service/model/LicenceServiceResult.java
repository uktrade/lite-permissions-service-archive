package uk.gov.bis.lite.permissions.service.model;

/**
 * Wrapper for {@link uk.gov.bis.lite.permissions.service.LicenceService} service calls
 * @param <T> Type to wrap
 */
public abstract class LicenceServiceResult<T> {
  public enum Status {
    OK,
    USER_ID_NOT_FOUND
  }

  private final Status status;

  private final T result;

  LicenceServiceResult (Status status, T result) {
    this.status = status;
    this.result = result;
  }

  public Status getStatus() {
    return this.status;
  }

  public boolean isOk() {
    return status == Status.OK;
  }

  public T getResult() {
    return this.result;
  }
}

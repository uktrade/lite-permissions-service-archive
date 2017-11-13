package uk.gov.bis.lite.permissions.service.model;

public abstract class ServiceResult<T> {
  public enum Status {
    OK,
    USER_ID_NOT_FOUND
  }

  private final Status status;

  private final T result;

  public ServiceResult (Status status, T result) {
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

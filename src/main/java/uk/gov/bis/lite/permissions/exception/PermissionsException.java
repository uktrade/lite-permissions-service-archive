package uk.gov.bis.lite.permissions.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class PermissionsException extends RuntimeException {

  public PermissionsException(String message) {
    super(message);
  }

  public PermissionsException(String message, Throwable cause) {
    super(message, cause);
  }

  public static class ServiceExceptionMapper
      implements ExceptionMapper<PermissionsException>, ErrorResponse {

    private static final int STATUS_INTERNAL_SERVER_ERROR = 500;

    @Override
    public Response toResponse(PermissionsException exception) {
      return buildResponse(exception.getMessage(), STATUS_INTERNAL_SERVER_ERROR);
    }

  }

}
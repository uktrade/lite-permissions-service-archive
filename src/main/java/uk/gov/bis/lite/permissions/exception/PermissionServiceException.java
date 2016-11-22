package uk.gov.bis.lite.permissions.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class PermissionServiceException extends RuntimeException {

  public PermissionServiceException(String message) {
    super(message);
  }

  public PermissionServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public static class ServiceExceptionMapper
      implements ExceptionMapper<PermissionServiceException>, ErrorResponse {

    private static final int STATUS_INTERNAL_SERVER_ERROR = 500;

    @Override
    public Response toResponse(PermissionServiceException e) {
      return buildResponse(e.getMessage(), STATUS_INTERNAL_SERVER_ERROR);
    }

  }

}
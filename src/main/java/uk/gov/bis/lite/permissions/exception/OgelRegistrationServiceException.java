package uk.gov.bis.lite.permissions.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class OgelRegistrationServiceException extends WebApplicationException {
  public OgelRegistrationServiceException(String message) {
    super("OgelRegistrationService Exception: " + message, Response.Status.BAD_REQUEST);
  }

  public OgelRegistrationServiceException(String message, Throwable cause) {
    super("OgelRegistrationService Exception: " + message, cause, Response.Status.BAD_REQUEST);
  }
}

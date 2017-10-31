package uk.gov.bis.lite.permissions.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class LicenceServiceException extends WebApplicationException {
  public LicenceServiceException(String message) {
    super("LicenceService Exception: " + message, Response.Status.BAD_REQUEST);
  }

  public LicenceServiceException(String message, Throwable cause) {
    super("LicenceService Exception: " + message, cause, Response.Status.BAD_REQUEST);
  }
}

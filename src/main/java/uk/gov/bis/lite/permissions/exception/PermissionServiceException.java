package uk.gov.bis.lite.permissions.exception;

import javax.ws.rs.WebApplicationException;

public class PermissionServiceException extends WebApplicationException {

  public PermissionServiceException(String message) {
    super(message);
  }

}

package uk.gov.bis.lite.permissions.spire.adapters;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class SpireLicenceAdapterException extends WebApplicationException {

  /**
   * SpireLicenceAdapterException
   *
   * @param info information on exception
   */
  public SpireLicenceAdapterException(String info) {
    super("SpireLicence Adapter Exception: " + info, Response.Status.BAD_REQUEST);
  }

  /**
   * SpireLicenceAdapterException
   *
   * @param info  information on exception
   * @param cause the cause
   */
  public SpireLicenceAdapterException(String info, Throwable cause) {
    super("SpireLicence Adapter Exception: " + info, cause, Response.Status.BAD_REQUEST);
  }
}

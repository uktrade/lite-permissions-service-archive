package uk.gov.bis.lite.permissions.exception;

import io.dropwizard.jersey.errors.ErrorMessage;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class SpireFailReasonException extends RuntimeException {

  private OgelSubmission.FailReason failReason;

  /**
   * SpireForbiddenException
   *
   * @param failReason information on exception
   */
  public SpireFailReasonException(OgelSubmission.FailReason failReason, String message) {
    super("FailReason: " + failReason.name() + " - " + message);
    this.failReason = failReason;
  }

  public OgelSubmission.FailReason getFailReason() {
    return failReason;
  }

  /**
   * Provided for Dropwizard/Jersey integration
   */
  public static class ServiceExceptionMapper implements ExceptionMapper<SpireFailReasonException> {

    @Override
    public Response toResponse(SpireFailReasonException exception) {
      return Response.status(400).entity(new ErrorMessage(400, exception.getMessage())).build();
    }
  }
}

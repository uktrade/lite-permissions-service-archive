package uk.gov.bis.lite.permissions.exception;

import uk.gov.bis.lite.permissions.model.OgelSubmission;

import javax.ws.rs.WebApplicationException;

public class SpireFailReasonException extends WebApplicationException {

  private final OgelSubmission.FailReason failReason;

  /**
   * SpireForbiddenException
   *
   * @param failReason information on exception
   */
  public SpireFailReasonException(OgelSubmission.FailReason failReason, String message) {
    super("FailReason: " + failReason.name() + " - " + message, 400);
    this.failReason = failReason;
  }

  public OgelSubmission.FailReason getFailReason() {
    return failReason;
  }
}

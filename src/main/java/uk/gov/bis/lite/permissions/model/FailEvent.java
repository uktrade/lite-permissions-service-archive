package uk.gov.bis.lite.permissions.model;

import uk.gov.bis.lite.permissions.service.ProcessSubmissionServiceImpl;

public class FailEvent {

  private final OgelSubmission.FailReason failReason;
  private final ProcessSubmissionServiceImpl.Origin origin;
  private final String message;

  public FailEvent(OgelSubmission.FailReason failReason, ProcessSubmissionServiceImpl.Origin origin, String message) {
    this.failReason = failReason;
    this.origin = origin;
    this.message = message;
  }

  public OgelSubmission.FailReason getFailReason() {
    return failReason;
  }

  public ProcessSubmissionServiceImpl.Origin getOrigin() {
    return origin;
  }

  public String getMessage() {
    return message;
  }

}

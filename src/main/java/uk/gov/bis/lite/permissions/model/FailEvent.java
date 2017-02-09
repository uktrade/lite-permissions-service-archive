package uk.gov.bis.lite.permissions.model;


import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.service.ProcessSubmissionServiceImpl;

public class FailEvent {

  private CallbackView.FailReason failReason;
  private ProcessSubmissionServiceImpl.Origin origin;
  private String message;

  public FailEvent() {
  }

  public FailEvent(CallbackView.FailReason failReason, ProcessSubmissionServiceImpl.Origin origin, String message) {
    this.failReason = failReason;
    this.origin = origin;
    this.message = message;
  }

  public CallbackView.FailReason getFailReason() {
    return failReason;
  }

  public void setFailReason(CallbackView.FailReason failReason) {
    this.failReason = failReason;
  }

  public ProcessSubmissionServiceImpl.Origin getOrigin() {
    return origin;
  }

  public void setOrigin(ProcessSubmissionServiceImpl.Origin origin) {
    this.origin = origin;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}

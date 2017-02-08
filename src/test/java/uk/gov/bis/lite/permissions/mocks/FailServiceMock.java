package uk.gov.bis.lite.permissions.mocks;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.FailService;
import uk.gov.bis.lite.permissions.service.FailServiceImpl;

@Singleton
public class FailServiceMock implements FailService {

  private int failServiceCallCount = 0;

  private CallbackView.FailReason lastFailReason = null;
  private FailServiceImpl.Origin lastOrigin = null;

  @Override
  public void failWithMessage(OgelSubmission sub, CallbackView.FailReason failReason, FailServiceImpl.Origin origin, String message) {
    failServiceCallCount++;
    lastFailReason = failReason;
    lastOrigin = origin;
  }

  public int getFailServiceCallCount() {
    return failServiceCallCount;
  }

  public void resetAll() {
    this.lastFailReason = null;
    this.lastOrigin = null;
    this.failServiceCallCount = 0;
  }

  public CallbackView.FailReason getLastFailReason() {
    return lastFailReason;
  }

  public FailServiceImpl.Origin getLastOrigin() {
    return lastOrigin;
  }
}

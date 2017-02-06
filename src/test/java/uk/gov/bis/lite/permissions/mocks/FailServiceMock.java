package uk.gov.bis.lite.permissions.mocks;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.FailService;
import uk.gov.bis.lite.permissions.service.FailServiceImpl;

@Singleton
public class FailServiceMock implements FailService {

  private int failServiceCallCount = 0;

  @Override
  public void fail(OgelSubmission sub, CallbackView.FailReason failReason, FailServiceImpl.Origin origin) {
    failServiceCallCount++;
  }

  @Override
  public void failWithMessage(OgelSubmission sub, CallbackView.FailReason failReason, FailServiceImpl.Origin origin, String message) {
    failServiceCallCount++;
  }

  public int getFailServiceCallCount() {
    return failServiceCallCount;
  }

  public void resetFailServiceCallCount() {
    this.failServiceCallCount = 0;
  }
}

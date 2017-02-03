package uk.gov.bis.lite.permissions.mocks;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.CallbackService;

@Singleton
public class CallbackServiceMock implements CallbackService {

  @Override
  public void completeCallback(OgelSubmission sub) {

  }
}

package uk.gov.bis.lite.permissions.mocks;

import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.FailService;
import uk.gov.bis.lite.permissions.service.FailServiceImpl;

public class FailServiceMock implements FailService {

  @Override
  public void fail(OgelSubmission sub, CallbackView.FailReason failReason, FailServiceImpl.Origin origin) {

  }

  @Override
  public void failWithMessage(OgelSubmission sub, CallbackView.FailReason failReason, FailServiceImpl.Origin origin, String message) {

  }
}

package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

public interface FailService {

  void fail(OgelSubmission sub, CallbackView.FailReason failReason, FailServiceImpl.Origin origin);

  void failWithMessage(OgelSubmission sub, CallbackView.FailReason failReason, FailServiceImpl.Origin origin, String message);
}

package uk.gov.bis.lite.permissions.spire;

import uk.gov.bis.lite.common.spire.client.errorhandler.ErrorNodeErrorHandler;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.exception.SpireFailReasonException;

public class OgelErrorNodeErrorHandler extends ErrorNodeErrorHandler {

  public OgelErrorNodeErrorHandler() {}

  public void handleError(String errorText) {
    if (errorText.contains("BLACKLISTED")) {
      throw new SpireFailReasonException(CallbackView.FailReason.BLACKLISTED, errorText);
    } else if (errorText.contains("USER_LACKS_SITE_PRIVILEGES") || errorText.contains("USER_LACKS_PRIVILEGES")) {
      throw new SpireFailReasonException(CallbackView.FailReason.PERMISSION_DENIED, errorText);
    } else if (errorText.contains("SITE_ALREADY_REGISTERED")) {
      throw new SpireFailReasonException(CallbackView.FailReason.SITE_ALREADY_REGISTERED, errorText);
    } else {
      throw new SpireFailReasonException(CallbackView.FailReason.ENDPOINT_ERROR, errorText);
    }
  }

}

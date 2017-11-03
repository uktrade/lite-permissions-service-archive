package uk.gov.bis.lite.permissions.spire.errorhandlers;

import uk.gov.bis.lite.common.spire.client.errorhandler.ErrorNodeErrorHandler;
import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;
import uk.gov.bis.lite.permissions.spire.exceptions.SpireUserNotFoundException;

public class LicenceErrorHandler extends ErrorNodeErrorHandler {

  public LicenceErrorHandler() {
    super("//*[local-name()='LICENCE_LIST']");
  }

  @Override
  public void handleError(String errorText) {
    if (errorText.matches("Web user account for provided (userId|loginId) not found.")) {
      throw new SpireUserNotFoundException("User not found: \"" + errorText + "\"");
    } else {
      throw new SpireClientException("Unhandled error: \"" + errorText + "\"");
    }
  }
}

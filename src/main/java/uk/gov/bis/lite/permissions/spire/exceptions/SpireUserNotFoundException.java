package uk.gov.bis.lite.permissions.spire.exceptions;

import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;

public class SpireUserNotFoundException extends SpireClientException {
  public SpireUserNotFoundException(String info) {
    super(info);
  }
}

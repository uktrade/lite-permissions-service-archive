package uk.gov.bis.lite.spire.client.model;

import javax.xml.soap.SOAPMessage;

public class SpireResponse {

  private SOAPMessage message;

  public SpireResponse(SOAPMessage message) {
    this.message = message;
  }

  public SOAPMessage getMessage() {
    return message;
  }

  public void setMessage(SOAPMessage message) {
    this.message = message;
  }

}

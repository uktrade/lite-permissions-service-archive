package uk.gov.bis.lite.spire;

import javax.xml.soap.SOAPMessage;

public class SpireResponse {

  private SpireClient.Endpoint endpoint;
  private SOAPMessage message;

  public SpireResponse(SOAPMessage message, SpireClient.Endpoint endpoint) {
    this.message = message;
    this.endpoint = endpoint;
  }

  public SOAPMessage getMessage() {
    return message;
  }

  public void setMessage(SOAPMessage message) {
    this.message = message;
  }

  public SpireClient.Endpoint getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(SpireClient.Endpoint endpoint) {
    this.endpoint = endpoint;
  }
}

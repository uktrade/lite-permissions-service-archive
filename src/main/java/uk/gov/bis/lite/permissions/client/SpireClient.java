package uk.gov.bis.lite.permissions.client;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

class SpireClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpireClient.class);

  private String soapUrl;
  private String soapClientUserName;
  private String soapClientPassword;
  private boolean logRequest = true;
  private boolean logResponse = true;

  SpireClient(String soapUrl, String clientUserName, String clientPassword) {
    this.soapUrl = soapUrl;
    this.soapClientUserName = clientUserName;
    this.soapClientPassword = clientPassword;
  }

  SOAPMessage getRequest(String namespace, String childName) {
    try {
      MessageFactory messageFactory = MessageFactory.newInstance();
      SOAPMessage soapMessage = messageFactory.createMessage();

      SOAPPart soapPart = soapMessage.getSOAPPart();
      SOAPEnvelope envelope = soapPart.getEnvelope();
      envelope.addNamespaceDeclaration("spir", "http://www.fivium.co.uk/fox/webservices/ispire/" + namespace);

      SOAPBody soapBody = envelope.getBody();
      soapBody.addChildElement(childName, "spir");

      MimeHeaders headers = soapMessage.getMimeHeaders();
      String authorization = Base64.getEncoder().encodeToString((soapClientUserName + ":" + soapClientPassword).getBytes("utf-8"));
      headers.addHeader("Authorization", "Basic " + authorization);
      soapMessage.saveChanges();
      return soapMessage;
    } catch (SOAPException e) {
      throw new RuntimeException("An error occurred creating the SOAP request for retrieving Customer Information from Spire", e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported Encoding type", e);
    }
  }

  void addChild(SOAPMessage soapMessage, String childElementName, String childElementText) {
    try {
      SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
      SOAPElement getCompaniesElement = (SOAPElement) body.getChildElements().next();
      SOAPElement childElement = getCompaniesElement.addChildElement(childElementName);
      childElement.addTextNode(childElementText);
      soapMessage.saveChanges();
    } catch (SOAPException e) {
      throw new RuntimeException("An error occurred adding child element", e);
    }
  }

  SOAPMessage getResponse(SOAPMessage request) {
    if (logRequest) {
      log("request", request);
    }
    SOAPMessage response = executeRequest(request);
    if (logResponse) {
      log("response", response);
    }
    return response;
  }

  private SOAPMessage executeRequest(SOAPMessage soap) {
    SOAPConnectionFactory soapConnectionFactory;
    SOAPConnection soapConnection = null;
    try {
      soapConnectionFactory = SOAPConnectionFactory.newInstance();
      soapConnection = soapConnectionFactory.createConnection();
      final Stopwatch stopwatch = Stopwatch.createStarted();
      SOAPMessage response = soapConnection.call(soap, soapUrl);
      stopwatch.stop();
      LOGGER.info("Spire list retrieved in " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds ");
      return response;
    } catch (SOAPException e) {
      throw new RuntimeException("An error occurred establishing the connection with SOAP client", e);
    } finally {
      if (soapConnection != null) {
        try {
          soapConnection.close();
        } catch (SOAPException e) {
          LOGGER.error("An error occurred closing the SOAP connection. ", e);
        }
      }
    }
  }

  private void log(String type, SOAPMessage soapMessage) {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      soapMessage.writeTo(outputStream);
      LOGGER.info(type + ": " + outputStream.toString());
    } catch (IOException | SOAPException e) {
      LOGGER.error("error", e);
    }
  }
}

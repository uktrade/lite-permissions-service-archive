package uk.gov.bis.lite.permissions.spireclient;

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
    return doGetRequest(namespace, childName, false);
  }

  SOAPMessage getSpirRequest(String namespace, String childName) {
    return doGetRequest(namespace, childName, true);
  }

  private SOAPMessage doGetRequest(String namespace, String childName, boolean includeSpir) {
    try {
      MessageFactory messageFactory = MessageFactory.newInstance();
      SOAPMessage message = messageFactory.createMessage();

      SOAPPart part = message.getSOAPPart();
      SOAPEnvelope envelope = part.getEnvelope();
      envelope.addNamespaceDeclaration("spir", "http://www.fivium.co.uk/fox/webservices/ispire/" + namespace);
      SOAPBody soapBody = envelope.getBody();

      if (includeSpir) {
        soapBody.addChildElement(childName, "spir");
      } else {
        soapBody.addChildElement(childName);
      }

      MimeHeaders headers = message.getMimeHeaders();
      String authorization = Base64.getEncoder().encodeToString((soapClientUserName + ":" + soapClientPassword).getBytes("utf-8"));
      headers.addHeader("Authorization", "Basic " + authorization);
      message.saveChanges();
      return message;
    } catch (SOAPException e) {
      throw new RuntimeException("An error occurred creating the SOAP request for retrieving Customer Information from Spire", e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported Encoding type", e);
    }
  }

  void addChild(SOAPMessage message, String childName, String childText) {
    try {
      SOAPBody body = message.getSOAPPart().getEnvelope().getBody();
      SOAPElement parent = (SOAPElement) body.getChildElements().next();
      SOAPElement child = parent.addChildElement(childName);
      child.addTextNode(childText);
      message.saveChanges();
    } catch (SOAPException e) {
      throw new RuntimeException("An error occurred adding child element", e);
    }
  }

  void addChildList(SOAPMessage message, String listName, String elementName, String childName, String childText) {
    try {
      SOAPBody body = message.getSOAPPart().getEnvelope().getBody();
      SOAPElement parent = (SOAPElement) body.getChildElements().next();
      SOAPElement list = parent.addChildElement(listName);
      SOAPElement element = list.addChildElement(elementName);
      SOAPElement child = element.addChildElement(childName);
      child.addTextNode(childText);
      message.saveChanges();
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

  private SOAPMessage executeRequest(SOAPMessage message) {
    SOAPConnectionFactory connectionFactory;
    SOAPConnection connection = null;
    try {
      connectionFactory = SOAPConnectionFactory.newInstance();
      connection = connectionFactory.createConnection();
      final Stopwatch stopwatch = Stopwatch.createStarted();
      SOAPMessage response = connection.call(message, soapUrl);
      stopwatch.stop();
      LOGGER.info("Spire list retrieved in " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds ");
      return response;
    } catch (SOAPException e) {
      throw new RuntimeException("An error occurred establishing the connection with SOAP client", e);
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (SOAPException e) {
          LOGGER.error("An error occurred closing the SOAP connection. ", e);
        }
      }
    }
  }

  private void log(String type, SOAPMessage message) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      message.writeTo(out);
      LOGGER.info(type + ": " + out.toString());
    } catch (IOException | SOAPException e) {
      LOGGER.error("error", e);
    }
  }
}

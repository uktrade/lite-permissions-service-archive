package uk.gov.bis.lite.permissions.spire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class SpireServiceClient  {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpireServiceClient.class);

  private String url;
  private String username;
  private String password;
  private String NAMESPACE_PREFIX = "spir";
  private String NAMESPACE_URI = "http://www.fivium.co.uk/fox/webservices/ispire/";

  private boolean logRequest = true;
  private boolean logResponse = true;

  SpireServiceClient(String url, String username, String password) {
    this.url = url;
    this.username = username;
    this.password = password;
  }

  SOAPMessage initRequest(String namespace, String childName) {
    return doGetRequest(namespace, childName, false);
  }

  SOAPMessage executeRequest(SOAPMessage request, String target) {
    if (logRequest) {
      log("request", request);
    }
    SOAPMessage response = doExecuteRequest(request, target);
    if (logResponse) {
      log("response", response);
    }
    return response;
  }

  private SOAPMessage doGetRequest(String namespace, String childName, boolean includeSpir) {
    try {
      SOAPMessage message = MessageFactory.newInstance().createMessage();
      addNamespace(message, namespace);
      SOAPBody soapBody = message.getSOAPPart().getEnvelope().getBody();

      if (includeSpir) {
        soapBody.addChildElement(childName, NAMESPACE_PREFIX);
      } else {
        soapBody.addChildElement(childName);
      }

      addAuthHeader(message);
      message.saveChanges();
      return message;
    } catch (SOAPException e) {
      throw new RuntimeException("An error occurred creating the SOAP request for retrieving Customer Information from Spire", e);
    }
  }

  private SOAPMessage doExecuteRequest(SOAPMessage message, String target) {
    SOAPConnectionFactory connectionFactory;
    SOAPConnection connection = null;
    try {
      connectionFactory = SOAPConnectionFactory.newInstance();
      connection = connectionFactory.createConnection();
      SOAPMessage response = connection.call(message, url + target);
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

  void addChild(SOAPMessage message, String childName, String childText) {
    try {
      SOAPElement parent = (SOAPElement) message.getSOAPPart().getEnvelope().getBody().getChildElements().next();
      SOAPElement child = parent.addChildElement(childName);
      child.addTextNode(childText);
      message.saveChanges();
    } catch (SOAPException e) {
      throw new RuntimeException("An error occurred adding child element", e);
    }
  }

  void addChildList(SOAPMessage message, String listName, String elementName, String childName, String childText) {
    try {
      SOAPElement parent = (SOAPElement) message.getSOAPPart().getEnvelope().getBody().getChildElements().next();
      SOAPElement list = parent.addChildElement(listName);
      SOAPElement element = list.addChildElement(elementName);
      SOAPElement child = element.addChildElement(childName);
      child.addTextNode(childText);
      message.saveChanges();
    } catch (SOAPException e) {
      throw new RuntimeException("An error occurred adding child element", e);
    }
  }

  SOAPMessage getSpirRequest(String namespace, String childName) {
    return doGetRequest(namespace, childName, true);
  }


  private void addNamespace(SOAPMessage message, String namespace) {
    try {
      message.getSOAPPart().getEnvelope().addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URI + namespace);
    } catch (SOAPException e) {
      e.printStackTrace();
    }
  }

  private void addAuthHeader(SOAPMessage message) {
    MimeHeaders headers = message.getMimeHeaders();
    String authorization = null;
    try {
      authorization = Base64.getEncoder().encodeToString((username + ":" + password).getBytes("utf-8"));
      headers.addHeader("Authorization", "Basic " + authorization);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  private void log(String type, SOAPMessage message) {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      message.writeTo(byteArrayOutputStream);
      LOGGER.warn(type + ": " + byteArrayOutputStream.toString());
    } catch (IOException | SOAPException e) {
      LOGGER.error("error", e);
    }
  }
}

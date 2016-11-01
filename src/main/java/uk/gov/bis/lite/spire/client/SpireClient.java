package uk.gov.bis.lite.spire.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.spire.client.parser.SpireParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class SpireClient<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpireClient.class);

  private SpireParser<T> parser;
  private String nameSpace;
  private String requestChildName;
  private boolean useSpirePrefix;
  private String username;
  private String password;
  private String url;

  public SpireClient(SpireParser<T> parser) {
    this.parser = parser;
  }

  public T getResult(SpireRequest request) {
    SpireResponse spireResponse = getSpireResponse(request, nameSpace);

    // Check for SoapResponse Errors - throws SpireException if found
    spireResponse.checkForErrors();

    return parser.parseResponse(spireResponse);
  }

  public void setConfig(String nameSpace, String requestChildName, boolean useSpirePrefix) {
    this.nameSpace = nameSpace;
    this.requestChildName = requestChildName;
    this.useSpirePrefix = useSpirePrefix;
  }

  public void setSpireConfig(String username, String password, String url) {
    this.username = username;
    this.password = password;
    this.url = url;
  }

  public SpireRequest createRequest() {
    return new SpireRequest(createRequestSoapMessage(nameSpace, requestChildName, useSpirePrefix));
  }

  private SpireResponse getSpireResponse(SpireRequest request, String urlSuffix) {
    logSoapMessage("request", request.getSoapMessage());
    SOAPMessage response = doExecuteRequest(request, urlSuffix);
    logSoapMessage("response", response);
    return new SpireResponse(response);
  }

  private SOAPMessage createRequestSoapMessage(String namespace, String childName, boolean withSpirPrefix) {
    try {
      SOAPMessage message = MessageFactory.newInstance().createMessage();
      addNamespace(message, namespace);
      SOAPBody soapBody = message.getSOAPPart().getEnvelope().getBody();
      if (withSpirPrefix) {
        soapBody.addChildElement(childName, SpireName.SPIR_PREFIX);
      } else {
        soapBody.addChildElement(childName);
      }
      addAuthorizationHeader(message);
      message.saveChanges();
      return message;
    } catch (SOAPException e) {
      throw new RuntimeException("Error occurred creating the SOAP request for retrieving Customer Information from Spire", e);
    }
  }

  private void addNamespace(SOAPMessage message, String namespace) {
    try {
      message.getSOAPPart().getEnvelope().addNamespaceDeclaration(
          SpireName.SPIR_PREFIX,
          SpireName.NAMESPACE_URI + namespace);
    } catch (SOAPException e) {
      e.printStackTrace();
    }
  }

  private void addAuthorizationHeader(SOAPMessage message) {
    MimeHeaders headers = message.getMimeHeaders();
    try {
      String authorization = Base64.getEncoder().encodeToString((username + ":" + password).getBytes("utf-8"));
      headers.addHeader("Authorization", "Basic " + authorization);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  private SOAPMessage doExecuteRequest(SpireRequest request, String urlSuffix) {
    SOAPConnection conn = null;
    try {
      conn = SOAPConnectionFactory.newInstance().createConnection();
      return conn.call(request.getSoapMessage(), url + urlSuffix);
    } catch (SOAPException e) {
      throw new RuntimeException("Error occurred establishing connection with SOAP client", e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SOAPException e) {
          LOGGER.error("Error occurred closing SOAP connection. ", e);
        }
      }
    }
  }

  private void logSoapMessage(String type, SOAPMessage message) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      message.writeTo(out);
      LOGGER.info(type + ": " + out.toString());
    } catch (IOException | SOAPException e) {
      LOGGER.error("error", e);
    }
  }

}

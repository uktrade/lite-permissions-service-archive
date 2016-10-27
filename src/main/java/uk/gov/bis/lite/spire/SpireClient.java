package uk.gov.bis.lite.spire;

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
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class SpireClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpireClient.class);

  private String username;
  private String password;
  private String url;

  private String SPIR_PREFIX = "spir";
  private String NAMESPACE_URI = "http://www.fivium.co.uk/fox/webservices/ispire/";

  public enum Endpoint {
    CREATE_OGEL_APP, CREATE_LITE_SAR, CREATE_SITE_FOR_SAR, EDIT_USER_ROLES, COMPANY_SITES, COMPANIES;
  }

  public void init(String username, String password, String url) {
    this.username = username;
    this.password = password;
    this.url = url;
  }

  public SpireRequest createRequest(Endpoint endpoint) {
    SpireRequest request = new SpireRequest(endpoint);
    switch (endpoint) {
      case CREATE_SITE_FOR_SAR:
        request.setEndpointTarget(SpireName.CSFS_NAME_SPACE);
        request.setSoapMessage(doGetSoapRequest(SpireName.CSFS_NAME_SPACE, SpireName.CSFS_REQUEST_CHILD, false));
        break;
      case CREATE_LITE_SAR:
        request.setEndpointTarget(SpireName.CLS_NAME_SPACE);
        request.setSoapMessage(doGetSoapRequest(SpireName.CLS_NAME_SPACE, SpireName.CLS_REQUEST_CHILD, false));
        break;
      case EDIT_USER_ROLES:
        request.setEndpointTarget(SpireName.EUR_NAME_SPACE);
        request.setSoapMessage(doGetSoapRequest(SpireName.EUR_NAME_SPACE, SpireName.EUR_REQUEST_CHILD, false));
        break;
      case CREATE_OGEL_APP:
        request.setEndpointTarget(SpireName.COA_NAME_SPACE);
        request.setSoapMessage(doGetSoapRequest(SpireName.COA_NAME_SPACE, SpireName.COA_REQUEST_CHILD, false));
        break;
      case COMPANY_SITES:
        request.setEndpointTarget(SpireName.COMPANY_SITES_NAME_SPACE);
        request.setSoapMessage(doGetSoapRequest(SpireName.COMPANY_SITES_NAME_SPACE, SpireName.COMPANY_SITES_REQUEST_CHILD, true));
        break;
      case COMPANIES:
        request.setEndpointTarget(SpireName.COMPANIES_NAME_SPACE);
        request.setSoapMessage(doGetSoapRequest(SpireName.COMPANIES_NAME_SPACE, SpireName.COMPANIES_REQUEST_CHILD, true));
        break;
      default:
    }
    return request;
  }

  public SpireResponse sendRequest(SpireRequest request) {
    logSoapMessage("request", request.getSoapMessage());
    SOAPMessage response = doExecuteRequest(request);
    logSoapMessage("response", response);
    return new SpireResponse(response, request.getEndpoint());
  }

  private SOAPMessage doGetSoapRequest(String namespace, String childName, boolean withSpirPrefix) {
    try {
      SOAPMessage message = MessageFactory.newInstance().createMessage();
      addNamespace(message, namespace);
      SOAPBody soapBody = message.getSOAPPart().getEnvelope().getBody();
      if (withSpirPrefix) {
        soapBody.addChildElement(childName, SPIR_PREFIX);
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
      message.getSOAPPart().getEnvelope().addNamespaceDeclaration(SPIR_PREFIX, NAMESPACE_URI + namespace);
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

  private SOAPMessage doExecuteRequest(SpireRequest request) {
    SOAPConnection conn = null;
    try {
      conn = SOAPConnectionFactory.newInstance().createConnection();
      return conn.call(request.getSoapMessage(), url + request.getEndpointTarget());
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

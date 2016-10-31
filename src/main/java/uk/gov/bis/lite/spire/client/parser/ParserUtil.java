package uk.gov.bis.lite.spire.client.parser;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.bis.lite.spire.client.exception.SpireException;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class ParserUtil {

  protected static final Logger LOGGER = LoggerFactory.getLogger(SpireParser.class);

  private static final String ERROR = "ERROR";
  private static final String XPATH_EXP_RESPONSE = "//*[local-name()='RESPONSE']";

  public static void checkForErrors(SOAPMessage message) {
    throwResponseErrorSpireException(message);
    throwSoapFaultSpireException(message);
  }

  private static void throwSoapFaultSpireException(SOAPMessage message) {
    String faultString = "";
    try {
      SOAPFault fault = message.getSOAPBody().getFault();
      if (fault != null) {
        faultString = fault.getFaultString();
      }
    } catch (SOAPException e) {
      e.printStackTrace();
    }
    if (!StringUtils.isBlank(faultString)) {
      throw new SpireException("soap:Fault: [" + faultString + "]");
    }
  }

  private static void throwResponseErrorSpireException(SOAPMessage message) {
    try {
      SOAPBody soapBody = message.getSOAPBody();
      XPath xpath = XPathFactory.newInstance().newXPath();
      NodeList responseNodes = (NodeList) xpath.evaluate(XPATH_EXP_RESPONSE, soapBody, XPathConstants.NODESET);
      if (responseNodes != null) {
        Node first = responseNodes.item(0);
        if (first != null) {
          NodeList nodes = first.getChildNodes();
          Node errorNode = (Node) XPathFactory.newInstance().newXPath().evaluate(ERROR, nodes, XPathConstants.NODE);
          if (errorNode != null) {
            throw new SpireException("ERROR: [" + errorNode.getTextContent() + "]");
          }
        }
      }
    } catch (XPathExpressionException | SOAPException e) {
      LOGGER.warn("Exception: " + Throwables.getStackTraceAsString(e));
    }
  }
}

package uk.gov.bis.lite.spire.client.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import uk.gov.bis.lite.spire.client.SpireName;
import uk.gov.bis.lite.spire.client.model.SpireResponse;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


public class SingleResponseParser extends SpireParser<String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SingleResponseParser.class);

  private String responseElementName;

  public SingleResponseParser(String responseElementName) {
    this.responseElementName = responseElementName;
  }

  public String getResult(SpireResponse spireResponse) {
    SOAPMessage message = spireResponse.getMessage();
    NodeList nodes = getResponseElementNodes(message);
    return reduce(nodes, responseElementName);
  }

  private NodeList getResponseElementNodes(SOAPMessage message) {
    NodeList nodes = null;
    try {
      SOAPBody soapBody = message.getSOAPBody();
      XPath xpath = XPathFactory.newInstance().newXPath();
      NodeList nodeList = (NodeList) xpath.evaluate(SpireName.SAR_XPATH_EXP, soapBody, XPathConstants.NODESET);
      if (nodeList != null && nodeList.item(0) != null) {
        nodes = nodeList.item(0).getChildNodes();
      }
    } catch (SOAPException | XPathExpressionException e) {
      LOGGER.error("", e);
    }
    return nodes;
  }

}

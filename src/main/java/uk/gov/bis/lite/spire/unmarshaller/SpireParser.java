package uk.gov.bis.lite.spire.unmarshaller;


import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.bis.lite.spire.exception.SpireException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class SpireParser {

  protected static final Logger LOGGER = LoggerFactory.getLogger(SpireParser.class);

  private static final String ERROR = "ERROR";
  private static final String XPATH_EXP_RESPONSE = "//*[local-name()='RESPONSE']";

  protected void checkForErrors(SOAPMessage message) {
    throwResponseErrorSpireException(message);
    throwSoapFaultSpireException(message);
  }

  protected Optional<String> getValue(Node singleNode, XPath xpath, String name) {
    try {
      Node node = (Node) xpath.evaluate(name, singleNode, XPathConstants.NODE);
      if (node != null) {
        return Optional.of(node.getTextContent());
      }
    } catch (XPathExpressionException e) {
      throw new RuntimeException("Error occurred while parsing the SOAP response body", e);
    }
    return Optional.empty();
  }

  protected NodeList getNodeList(SOAPMessage message, String mainTag) {
    NodeList nodeList = null;
    try {
      SOAPBody soapBody = message.getSOAPBody();
      XPath xpath = XPathFactory.newInstance().newXPath();
      nodeList = (NodeList) xpath.evaluate(mainTag, soapBody, XPathConstants.NODESET);
    } catch (SOAPException | XPathExpressionException e) {
      throw new RuntimeException("An error occurred while extracting the SOAP Response Body", e);
    }
    return nodeList;
  }

  protected String reduce(NodeList nodes, String nodeName) {
    return list(nodes).stream()
        .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
        .filter(node -> node.getNodeName().equals(nodeName))
        .map(this::getText)
        .collect(Collectors.joining());
  }

  protected List<Node> list(NodeList nodeList) {
    List<Node> list = new ArrayList<>();
    for (int i = 0; i < nodeList.getLength(); i++) {
      list.add(nodeList.item(i));
    }
    return list;
  }

  private String getText(Node node) {
    StringBuilder reply = new StringBuilder();
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if ((isCharacterData(child) && !isComment(child)) || isEntityReference(child)) {
        reply.append(child.getNodeValue());
      } else if (isElementNode(child)) {
        reply.append(getText(child));
      }
    }
    return reply.toString();
  }

  private boolean isEntityReference(Node node) {
    return node instanceof EntityReference;
  }

  private boolean isComment(Node node) {
    return node instanceof Comment;
  }

  private boolean isCharacterData(Node node) {
    return node instanceof CharacterData;
  }

  private boolean isElementNode(Node node) {
    return node.getNodeType() == Node.ELEMENT_NODE;
  }

  private void throwSoapFaultSpireException(SOAPMessage message) {
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

  private void throwResponseErrorSpireException(SOAPMessage message) {
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

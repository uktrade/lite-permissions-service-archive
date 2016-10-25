package uk.gov.bis.lite.spireclient.spire;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.bis.lite.spireclient.model.SpireResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class Unmarshaller {

  private static final Logger LOGGER = LoggerFactory.getLogger(Unmarshaller.class);

  private static final String ERROR = "ERROR";

  public SpireResponse getSpireResponse(SOAPMessage message, String elementName, String expression) {
    try {
      final SOAPBody soapBody = message.getSOAPBody();
      XPath xpath = XPathFactory.newInstance().newXPath();
      NodeList nodeList = (NodeList) xpath.evaluate(expression, soapBody, XPathConstants.NODESET);
      if (nodeList != null && nodeList.item(0) != null) {
        return singleElementNodeSpireResponse(nodeList, xpath, elementName);
      } else {
        throwExceptionIfFault(message);
      }
    } catch (SOAPException | XPathExpressionException e) {
      throw new SpireException("Unmarshalling SOAP Response Body Error: " + Throwables.getStackTraceAsString(e));
    }
    return SpireResponse.error("Soap response has unrecognised content - see log");
  }

  private static String throwExceptionIfFault(SOAPMessage message) {
    String faultString = "";
    try {
      SOAPFault fault = message.getSOAPBody().getFault();
      faultString = fault.getFaultString();
    } catch (SOAPException e) {
      e.printStackTrace();
    }
    if(!StringUtils.isBlank(faultString)) {
      throw new SpireException("soap:Fault: [" + faultString + "]");
    }
    return faultString;
  }

  /**
   * Private Methods
   */
  private SpireResponse singleElementNodeSpireResponse(NodeList nodeList, XPath xpath, String nodeName) {
    NodeList nodes = nodeList.item(0).getChildNodes();
    SpireResponse spireResponse = checkResponse(nodes, xpath);
    if (!spireResponse.hasError()) {
      String reference = reduce(nodes, nodeName);
      if(reference != null && !reference.isEmpty()) {
        spireResponse.setRef(reference);
      }
    } else {
      throw new SpireException(spireResponse.getErrorMessage());
    }
    return spireResponse;
  }

  private SpireResponse checkResponse(NodeList nodes, XPath xpath) {
    SpireResponse spireResponse = new SpireResponse();
    try {
      Node node = (Node) xpath.evaluate(ERROR, nodes, XPathConstants.NODE);
      if (node != null) {
        spireResponse.setErrorMessage(node.getTextContent());
      }
    } catch (XPathExpressionException e) {
      spireResponse.setErrorMessage("XPathExpressionException - an error occurred while parsing the SOAP response body.");
    }
    return spireResponse;
  }

  private String reduce(NodeList nodes, String nodeName) {
    return list(nodes).stream()
        .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
        .filter(node -> node.getNodeName().equals(nodeName))
        .map(this::getText)
        .collect(Collectors.joining());
  }

  private List<Node> list(NodeList nodeList) {
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
}

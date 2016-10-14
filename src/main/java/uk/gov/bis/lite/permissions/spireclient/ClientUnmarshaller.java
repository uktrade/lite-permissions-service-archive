package uk.gov.bis.lite.permissions.spireclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class ClientUnmarshaller {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientUnmarshaller.class);

  public Optional<String> getResponse(SOAPMessage message, String elementName, String expression) {
    try {
      final SOAPBody soapBody = message.getSOAPBody();
      XPath xpath = XPathFactory.newInstance().newXPath();
      NodeList nodeList = (NodeList) xpath.evaluate(expression, soapBody, XPathConstants.NODESET);
      if (nodeList != null) {
        return singleElementNodeResult(nodeList, xpath, elementName);
      }
      return null;
    } catch (SOAPException | XPathExpressionException e) {
      throw new RuntimeException("An error occurred while extracting the SOAP Response Body", e);
    }
  }

  private Optional<String> singleElementNodeResult(NodeList nodeList, XPath xpath, String nodeName) {
    NodeList nodes = nodeList.item(0).getChildNodes();
    Optional<String> errorCheck = errorCheck(nodes, xpath);
    if (!errorCheck.isPresent()) {
      return Optional.of(reduce(nodes, nodeName));
    } else {
      LOGGER.error(errorCheck.get());
    }
    return Optional.empty();
  }

  public String reduce(NodeList nodes, String nodeName) {
    return list(nodes).stream()
        .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
        .filter(node -> node.getNodeName().equals(nodeName))
        .map(this::getText)
        .reduce("", (s, t) -> s + t);
  }

  public Optional<String> errorCheck(NodeList nodes, XPath xpath) {
    try {
      Node errorNode = (Node) xpath.evaluate("ERROR", nodes, XPathConstants.NODE);
      if (errorNode != null) {
        return Optional.of(errorNode.getTextContent());
      }
    } catch (XPathExpressionException e) {
      return Optional.of("XPathExpressionException - an error occurred while parsing the SOAP response body.");
    }
    return Optional.empty();
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
      if ((child instanceof CharacterData && !(child instanceof Comment)) || child instanceof EntityReference) {
        reply.append(child.getNodeValue());
      } else if (child.getNodeType() == Node.ELEMENT_NODE) {
        reply.append(getText(child));
      }
    }
    return reply.toString();
  }
}
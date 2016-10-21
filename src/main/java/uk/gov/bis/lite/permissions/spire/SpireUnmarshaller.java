package uk.gov.bis.lite.permissions.spire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.bis.lite.permissions.spireclient.ClientUnmarshaller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class SpireUnmarshaller  {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpireUnmarshaller.class);

  private static final String ERROR = "ERROR";

  public SpireRefResponse getResponse(SOAPMessage message, String elementName, String expression) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      NodeList nodeList = (NodeList) xpath.evaluate(expression, message.getSOAPBody(), XPathConstants.NODESET);
      if (nodeList != null && nodeList.item(0) != null) {
        return singleElementNodeSpireResponse(nodeList, xpath, elementName);
      }
    } catch (SOAPException | XPathExpressionException e) {
      //throw new RuntimeException("An error occurred while extracting the SOAP Response Body", e);
      return SpireRefResponse.error(e.getClass().getCanonicalName() + ": error occurred while extracting the SOAP Response Body");
    }
    return SpireRefResponse.error("");
  }

  private SpireRefResponse singleElementNodeSpireResponse(NodeList nodeList, XPath xpath, String nodeName) {
    NodeList nodes = nodeList.item(0).getChildNodes();
    SpireRefResponse spireRefResponse = checkResponse(nodes, xpath);
    if (!spireRefResponse.hasError()) {
      String reference = reduce(nodes, nodeName);
      if(reference != null && !reference.isEmpty()) {
        spireRefResponse.setRef(reference);
      }
    }
    return spireRefResponse;
  }

  private String reduce(NodeList nodes, String nodeName) {
    return list(nodes).stream()
        .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
        .filter(node -> node.getNodeName().equals(nodeName))
        .map(this::getText)
        .collect(Collectors.joining());
  }


  private SpireRefResponse checkResponse(NodeList nodes, XPath xpath) {
    SpireRefResponse spireRefResponse = new SpireRefResponse();
    try {
      Node errorNode = (Node) xpath.evaluate(ERROR, nodes, XPathConstants.NODE);
      if (errorNode != null) {
        spireRefResponse.setErrorMessage(errorNode.getTextContent());
      }
    } catch (XPathExpressionException e) {
      spireRefResponse.setErrorMessage("XPathExpressionException - an error occurred while parsing the SOAP response body.");
    }
    return spireRefResponse;
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

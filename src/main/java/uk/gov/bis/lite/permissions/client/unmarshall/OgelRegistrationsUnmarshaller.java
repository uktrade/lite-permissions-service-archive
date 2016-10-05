package uk.gov.bis.lite.permissions.client.unmarshall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.bis.lite.permissions.model.OgelReg;

import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

public class OgelRegistrationsUnmarshaller extends ServiceUnmarshaller {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelRegistrationsUnmarshaller.class);

  private static final String OGEL_TYPE_REF = "OGEL_TYPE_REF";
  private static final String SAR_REF = "SAR_REF";
  private static final String REGISTRATION_REF = "REGISTRATION_REF";
  private static final String REGISTRATION_DATE = "REGISTRATION_DATE";
  private static final String STATUS = "STATUS";
  private static final String ERROR = "ERROR";


  public List<OgelReg> execute(SOAPMessage message) {
    return (List<OgelReg>) super.execute(message, "//OGEL_REGISTRATION_LIST");
  }

  protected List<OgelReg> parseSoapBody(NodeList nodeList, XPath xpath) {

    List<OgelReg> regs = new ArrayList<>();
    NodeList nodes = getFirstNodeListItem(nodeList);
    checkForError(nodes, xpath);

    for (int i = 0; i < nodes.getLength(); i++) {
      OgelReg ogReg = new OgelReg();
      Node node = nodes.item(i).cloneNode(true);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        getNodeValue(node, xpath, OGEL_TYPE_REF).ifPresent(ogReg::setOgelTypeRef);
        getNodeValue(node, xpath, SAR_REF).ifPresent(ogReg::setSarRef);
        getNodeValue(node, xpath, REGISTRATION_REF).ifPresent(ogReg::setRegistrationRef);
        getNodeValue(node, xpath, REGISTRATION_DATE).ifPresent(ogReg::setRegistrationDate);
        getNodeValue(node, xpath, STATUS).ifPresent(ogReg::setStatus);
        regs.add(ogReg);
      }
    }

    return regs;
  }

  private NodeList getFirstNodeListItem(NodeList nodeList) {
    return nodeList.item(0).getChildNodes();
  }

  private void checkForError(NodeList nodeList, XPath xpath) {
    try {
      Node errorNode = (Node) xpath.evaluate(ERROR, nodeList, XPathConstants.NODE);
      if (errorNode != null) {
        throw new RuntimeException("Unexpected Error Occurred: " + errorNode.getTextContent());
      }
    } catch (XPathExpressionException e) {
      throw new RuntimeException("An error occurred while parsing the SOAP response body", e);
    }
  }
}

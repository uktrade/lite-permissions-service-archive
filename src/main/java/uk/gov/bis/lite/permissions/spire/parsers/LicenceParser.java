package uk.gov.bis.lite.permissions.spire.parsers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import uk.gov.bis.lite.common.spire.client.SpireResponse;
import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;
import uk.gov.bis.lite.common.spire.client.parser.SpireParser;
import uk.gov.bis.lite.permissions.spire.model.SpireLicence;

import java.util.List;
import java.util.Optional;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class LicenceParser implements SpireParser<List<SpireLicence>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenceParser.class);

  @Override
  public List<SpireLicence> parseResponse(SpireResponse spireResponse) {
    XPath xPath = XPathFactory.newInstance().newXPath();
    spireResponse.getElementChildNodesForList("//LICENCE_LIST")
        .stream()
        .map(node -> {
          Node clonedNode = node.cloneNode(true);
          if (StringUtils.equalsIgnoreCase(clonedNode.getNodeName(), "LICENCE")) {
            SpireLicence licence = new SpireLicence();
            getNodeValue(xPath, clonedNode, "LICENCE_REFERENCE").ifPresent(licence::setLicenceReference);
            getNodeValue(xPath, clonedNode, "ORIGINAL_APPLICATION_REFERENCE").ifPresent(licence::setOriginalApplicationReference);
          } else {
            LOGGER.warn("Unexpected element found while parsing the SOAP response body: \"{}\"", clonedNode.getNodeName());
            return null;
          }
        })
    return null;
  }

  private static Optional<String> getNodeValue(XPath xpath, Node node, String name){
    try {
      return Optional.ofNullable(((Node) xpath.evaluate(name, node, XPathConstants.NODE))).map(Node::getTextContent);
    } catch (XPathExpressionException e) {
      throw new SpireClientException("Error occurred while parsing the SOAP response body", e);
    }
  }
}

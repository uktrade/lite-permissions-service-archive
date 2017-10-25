package uk.gov.bis.lite.permissions.spire.parsers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.bis.lite.common.spire.client.SpireResponse;
import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;
import uk.gov.bis.lite.common.spire.client.parser.SpireParser;
import uk.gov.bis.lite.permissions.spire.model.SpireLicence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class LicenceParser implements SpireParser<List<SpireLicence>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenceParser.class);

  @Override
  public List<SpireLicence> parseResponse(SpireResponse spireResponse) {
    XPath xPath = XPathFactory.newInstance().newXPath();
    return spireResponse.getElementChildNodesForList("//LICENCE_LIST")
        .stream()
        .map(node -> {
          Node clonedNode = node.cloneNode(true);
          if (StringUtils.equalsIgnoreCase(clonedNode.getNodeName(), "LICENCE")) {
            SpireLicence licence = new SpireLicence();
            getNodeValue(xPath, clonedNode, "REFERENCE").ifPresent(licence::setReference);
            getNodeValue(xPath, clonedNode, "ORIGINAL_APPLICATION_REFERENCE").ifPresent(licence::setOriginalApplicationReference);
            getNodeValue(xPath, clonedNode, "EXPORTER_APPLICATION_REFERENCE").ifPresent(licence::setExporterApplicationReference);
            getNodeValue(xPath, clonedNode, "SAR_ID").ifPresent(licence::setSarId);
            getNodeValue(xPath, clonedNode, "SITE_ID").ifPresent(licence::setSiteId);
            getNodeValue(xPath, clonedNode, "TYPE").ifPresent(licence::setType);
            getNodeValue(xPath, clonedNode, "SUB_TYPE").ifPresent(licence::setSubType);
            getNodeValue(xPath, clonedNode, "ISSUE_DATE").ifPresent(licence::setIssueDate);
            getNodeValue(xPath, clonedNode, "EXPIRY_DATE").ifPresent(licence::setExpiryDate);
            getNodeValue(xPath, clonedNode, "STATUS").ifPresent(licence::setStatus);
            getNodeValue(xPath, clonedNode, "EXTERNAL_DOCUMENT_URL").ifPresent(licence::setExternalDocumentUrl);
            getChildNodes(xPath, clonedNode, "COUNTRY_LIST").ifPresent(countryNodeList -> {
              List<String> countryList = new ArrayList<>();
              for (int i = 0; i < countryNodeList.getLength(); i++) {
                Node countryNode = countryNodeList.item(i);
                if (StringUtils.equalsIgnoreCase(countryNode.getNodeName(), "COUNTRY")) {
                  countryList.add(countryNode.getNodeValue());
                }
              }
              licence.setCountryList(Collections.unmodifiableList(countryList));
            });
            return licence;
          } else {
            LOGGER.warn("Unexpected element found while parsing the SOAP response body: \"{}\"", clonedNode.getNodeName());
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private static Optional<String> getNodeValue(XPath xPath, Node node, String name){
    try {
      return Optional.ofNullable(((Node) xPath.evaluate(name, node, XPathConstants.NODE))).map(Node::getTextContent);
    } catch (XPathExpressionException e) {
      throw new SpireClientException("Error occurred while parsing the SOAP response body", e);
    }
  }

  private static Optional<NodeList> getChildNodes(XPath xPath, Node node, String name) {
    try {
      return Optional.ofNullable((Node) xPath.evaluate(name, node, XPathConstants.NODE)).map(Node::getChildNodes);
    } catch (XPathExpressionException e) {
      throw new SpireClientException("Error occurred while parsing the SOAP response body", e);
    }
  }
}

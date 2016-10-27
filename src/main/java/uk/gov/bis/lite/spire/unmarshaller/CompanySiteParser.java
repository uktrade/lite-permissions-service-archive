package uk.gov.bis.lite.spire.unmarshaller;


import static uk.gov.bis.lite.spire.SpireClient.Endpoint.COMPANY_SITES;
import static uk.gov.bis.lite.spire.SpireName.ADDRESS;
import static uk.gov.bis.lite.spire.SpireName.APPLICANT_TYPE;
import static uk.gov.bis.lite.spire.SpireName.COMPANY_NAME;
import static uk.gov.bis.lite.spire.SpireName.COMPANY_SITES_RESPONSE_LIST;
import static uk.gov.bis.lite.spire.SpireName.DIVISION;
import static uk.gov.bis.lite.spire.SpireName.OCCUPANCY_STATUS;
import static uk.gov.bis.lite.spire.SpireName.SAR_REF;
import static uk.gov.bis.lite.spire.SpireName.SITE_REF;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.bis.lite.spire.SpireResponse;
import uk.gov.bis.lite.spire.exception.SpireException;
import uk.gov.bis.lite.spire.model.SpireSite;

import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

public class CompanySiteParser extends SpireParser {

  public List<SpireSite> getSpireSites(SpireResponse spireResponse) {
    List<SpireSite> sites;
    SOAPMessage message = spireResponse.getMessage();
    if (spireResponse.getEndpoint().equals(COMPANY_SITES)) {
      checkForErrors(message); // throws SpireException if any error/soapFault found
      sites = parseCompanySites(getNodeList(message, COMPANY_SITES_RESPONSE_LIST));
    } else {
      throw new SpireException("Configuration issue: [CompanyParser must be called with a SpireRequest with a COMPANY_SITES endpoint]");
    }
    return sites;
  }

  private List<SpireSite> parseCompanySites(NodeList nodeList) {
    List<SpireSite> sites = new ArrayList<>();
    XPath xpath = XPathFactory.newInstance().newXPath();
    NodeList nodes = nodeList.item(0).getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      SpireSite site = new SpireSite();
      Node node = nodes.item(i).cloneNode(true);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        getValue(node, xpath, SITE_REF).ifPresent(site::setSiteRef);
        getValue(node, xpath, SAR_REF).ifPresent(site::setSarRef);
        getValue(node, xpath, COMPANY_NAME).ifPresent(site::setCompanyName);
        getValue(node, xpath, APPLICANT_TYPE).ifPresent(site::setApplicantType);
        getValue(node, xpath, ADDRESS).ifPresent(site::setAddress);
        getValue(node, xpath, DIVISION).ifPresent(site::setDivision);
        getValue(node, xpath, OCCUPANCY_STATUS).ifPresent(site::setOccupancyStatus);
        sites.add(site);
      }
    }
    return sites;
  }
}

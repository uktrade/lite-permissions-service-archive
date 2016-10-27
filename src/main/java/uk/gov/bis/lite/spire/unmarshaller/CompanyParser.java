package uk.gov.bis.lite.spire.unmarshaller;

import static uk.gov.bis.lite.spire.SpireClient.Endpoint.COMPANIES;
import static uk.gov.bis.lite.spire.SpireName.APPLICANT_TYPE;
import static uk.gov.bis.lite.spire.SpireName.COMPANIES_RESPONSE_LIST;
import static uk.gov.bis.lite.spire.SpireName.COMPANY_NUMBER;
import static uk.gov.bis.lite.spire.SpireName.COUNTRY_OF_ORIGIN;
import static uk.gov.bis.lite.spire.SpireName.NAME;
import static uk.gov.bis.lite.spire.SpireName.NATURE_OF_BUSINESS;
import static uk.gov.bis.lite.spire.SpireName.ORGANISATION_TYPE;
import static uk.gov.bis.lite.spire.SpireName.REGISTERED_ADDRESS;
import static uk.gov.bis.lite.spire.SpireName.REGISTRATION_STATUS;
import static uk.gov.bis.lite.spire.SpireName.SAR_REF;
import static uk.gov.bis.lite.spire.SpireName.SHORT_NAME;
import static uk.gov.bis.lite.spire.SpireName.WEBSITE_LIST;
import static uk.gov.bis.lite.spire.SpireName.WEBSITE_URL;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.bis.lite.spire.SpireResponse;
import uk.gov.bis.lite.spire.exception.SpireException;
import uk.gov.bis.lite.spire.model.SpireCompany;
import uk.gov.bis.lite.spire.model.SpireOrganisationType;
import uk.gov.bis.lite.spire.model.SpireWebsite;

import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class CompanyParser extends SpireParser {

  public List<SpireCompany> getSpireCompanies(SpireResponse spireResponse) {
    List<SpireCompany> companies;
    SOAPMessage message = spireResponse.getMessage();
    if (spireResponse.getEndpoint().equals(COMPANIES)) {
      checkForErrors(message); // throws SpireException if any error/soapFault found
      companies = parseCompanies(getNodeList(message, COMPANIES_RESPONSE_LIST));
    } else {
      throw new SpireException("Configuration issue: [CompanyParser must be called with a SpireRequest with a COMPANIES endpoint]");
    }
    return companies;
  }

  private List<SpireCompany> parseCompanies(NodeList nodeList) {
    List<SpireCompany> companies = new ArrayList<>();
    XPath xpath = XPathFactory.newInstance().newXPath();
    NodeList nodes = nodeList.item(0).getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      SpireCompany company = new SpireCompany();
      Node node = nodes.item(i).cloneNode(true);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        getValue(node, xpath, SAR_REF).ifPresent(company::setSarRef);
        getValue(node, xpath, NAME).ifPresent(company::setName);
        getValue(node, xpath, SHORT_NAME).ifPresent(company::setShortName);
        getValue(node, xpath, ORGANISATION_TYPE).ifPresent(v -> company.setSpireOrganisationType(SpireOrganisationType.valueOf(v)));
        getValue(node, xpath, COMPANY_NUMBER).ifPresent(company::setNumber);
        getValue(node, xpath, REGISTRATION_STATUS).ifPresent(company::setRegistrationStatus);
        getValue(node, xpath, REGISTERED_ADDRESS).ifPresent(company::setRegisteredAddress);
        getValue(node, xpath, APPLICANT_TYPE).ifPresent(company::setApplicantType);
        getValue(node, xpath, NATURE_OF_BUSINESS).ifPresent(company::setNatureOfBusiness);
        getValue(node, xpath, COUNTRY_OF_ORIGIN).ifPresent(company::setCountryOfOrigin);

        List<SpireWebsite> webSites = new ArrayList<>();
        Node websitesNode = null;
        try {
          websitesNode = (Node) xpath.evaluate(WEBSITE_LIST, node, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
          e.printStackTrace();
        }
        if (websitesNode != null) {
          SpireWebsite website = new SpireWebsite();
          NodeList webSitesChildren = websitesNode.getChildNodes();
          for (int k = 0; k < webSitesChildren.getLength(); k++) {
            Node websiteNode = webSitesChildren.item(k).cloneNode(true);
            getValue(websiteNode, xpath, WEBSITE_URL).ifPresent(website::setUrl);
            //getValue(websiteNode, xpath, WEBSITE_ACTION).ifPresent(website::setAction);
            webSites.add(website);
          }
        }
        company.setWebsites(webSites);
        companies.add(company);
      }
    }
    return companies;
  }
}

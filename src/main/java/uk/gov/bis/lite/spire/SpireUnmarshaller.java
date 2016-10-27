package uk.gov.bis.lite.spire;

import uk.gov.bis.lite.spire.model.SpireCompany;
import uk.gov.bis.lite.spire.model.SpireSite;
import uk.gov.bis.lite.spire.unmarshaller.CompanyParser;
import uk.gov.bis.lite.spire.unmarshaller.CompanySiteParser;
import uk.gov.bis.lite.spire.unmarshaller.ResponseElementParser;

import java.util.List;

import javax.xml.soap.SOAPMessage;

public class SpireUnmarshaller {

  private ResponseElementParser responseElementParser;
  private CompanyParser companyParser;
  private CompanySiteParser companySiteParser;

  public SpireUnmarshaller() {
    this.responseElementParser = new ResponseElementParser();
    this.companyParser = new CompanyParser();
    this.companySiteParser = new CompanySiteParser();
  }

  public List<SpireCompany> getSpireCompanies(SpireResponse spireResponse) {
    return companyParser.getSpireCompanies(spireResponse);
  }

  public List<SpireSite> getSpireSites(SpireResponse spireResponse) {
    return companySiteParser.getSpireSites(spireResponse);
  }

  public String getSingleResponseElementContent(SpireResponse spireResponse) {
    String content = "";
    SOAPMessage message = spireResponse.getMessage();
    switch (spireResponse.getEndpoint()) {
      case CREATE_SITE_FOR_SAR:
        content = responseElementParser.getSpireResponse(message, SpireName.CSFS_RESPONSE_ELEMENT);
        break;
      case CREATE_LITE_SAR:
        content = responseElementParser.getSpireResponse(message, SpireName.CLS_RESPONSE_ELEMENT);
        break;
      case EDIT_USER_ROLES:
        content = responseElementParser.getSpireResponse(message, SpireName.EUR_RESPONSE_ELEMENT);
        break;
      case CREATE_OGEL_APP:
        content = responseElementParser.getSpireResponse(message, SpireName.COA_RESPONSE_ELEMENT);
        break;
      default:
    }
    return content;
  }

}

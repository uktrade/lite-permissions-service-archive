package uk.gov.bis.lite.permissions.spire;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.spire.model.AddressItem;
import uk.gov.bis.lite.permissions.spire.model.CustomerItem;
import uk.gov.bis.lite.permissions.spire.model.OgelAppItem;
import uk.gov.bis.lite.permissions.spire.model.SiteItem;
import uk.gov.bis.lite.permissions.spire.model.UserRoleItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.soap.SOAPMessage;

import static uk.gov.bis.lite.permissions.spire.SpireNames.*;

public class SpireService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpireService.class);

  private String url;
  private String username;
  private String password;
  private SpireServiceClient client;
  private SpireUnmarshaller unmarshaller;
  private ObjectMapper mapper;
  private List<String> endPoints = new ArrayList<>();

  private static final String SAR_XPATH_EXP = "//*[local-name()='RESPONSE']";

  private static final String COA_NAME_SPACE = "SPIRE_CREATE_OGEL_APP";
  private static final String COA_VERSION = "1.0";
  private static final String COA_REQUEST_CHILD = "OGEL_DETAILS";
  private static final String COA_RESPONSE_ELEMENT = "SPIRE_REF";

  private static final String CLS_NAME_SPACE = "SPIRE_CREATE_LITE_SAR";
  private static final String CLS_VERSION = "1.1";
  private static final String CLS_REQUEST_CHILD = "SAR_DETAILS";
  private static final String CLS_RESPONSE_ELEMENT = "SAR_REF";

  private static final String CSFS_NAME_SPACE = "SPIRE_CREATE_SITE_FOR_SAR";
  private static final String CSFS_VERSION = "1.0";
  private static final String CSFS_REQUEST_CHILD = "SITE_DETAILS";
  private static final String CSFS_RESPONSE_ELEMENT = "SPIRE_REF";

  private static final String EUR_NAME_SPACE = "SPIRE_EDIT_USER_ROLES";
  private static final String EUR_VERSION = "1.1";
  private static final String EUR_REQUEST_CHILD = "USER_DETAILS";
  private static final String EUR_RESPONSE_ELEMENT = "RESULT";

  public void init(String username, String password, String url, String commaSeparatedEndpoints) {
    this.username = username;
    this.password = password;
    this.url = url;
    this.unmarshaller = new SpireUnmarshaller();
    this.client = new SpireServiceClient(url, username, password);
    this.mapper = new ObjectMapper();
    Collections.addAll(endPoints, commaSeparatedEndpoints.split("\\s*,\\s*"));
  }

  /**
   * CREATE_OGEL_APP (COA)
   */
  public SpireRefResponse createOgelApp(OgelAppItem item) {
    SOAPMessage request = client.initRequest(COA_NAME_SPACE, COA_REQUEST_CHILD);
    client.addChild(request, VERSION_NO, COA_VERSION);
    client.addChild(request, WUA_ID, item.getUserId());
    client.addChild(request, SAR_REF, item.getSarRef());
    client.addChild(request, SITE_REF, item.getSiteRef());
    client.addChildList(request, OGL_TYPE_LIST, OGL_TYPE, TYPE, item.getOgelType());
    SOAPMessage response = client.executeRequest(request, COA_NAME_SPACE);
    return unmarshaller.getResponse(response, COA_RESPONSE_ELEMENT, SAR_XPATH_EXP);
  }

  /**
   * CREATE_LITE_SAR (CLS)
   */
  public SpireRefResponse createLiteSar(CustomerItem item) {
    SOAPMessage request = client.initRequest(CLS_NAME_SPACE, CLS_REQUEST_CHILD);

    client.addChild(request, VERSION_NO, CLS_VERSION);
    client.addChild(request, WUA_ID, item.getUserId());
    client.addChild(request, CUSTOMER_NAME, item.getCustomerName());
    client.addChild(request, CUSTOMER_TYPE, item.getCustomerType());
    client.addChild(request, LITE_ADDRESS, getAddressItemJson(item.getAddressItem()));
    client.addChild(request, ADDRESS, getFriendlyAddress(item.getAddressItem()));
    client.addChild(request, COUNTRY_REF, item.getAddressItem().getCountry());
    client.addChild(request, WEBSITE, item.getWebsite());

    String companiesHouseNumber = item.getCompaniesHouseNumber();
    if(!StringUtils.isBlank(companiesHouseNumber)) {
      client.addChild(request, COMPANIES_HOUSE_NUMBER, companiesHouseNumber);
      client.addChild(request, COMPANIES_HOUSE_VALIDATED, item.getCompaniesHouseValidated().toString());
    }
    String eoriNumber = item.getEoriNumber();
    if(!StringUtils.isBlank(eoriNumber)) {
      client.addChild(request, EORI_NUMBER, eoriNumber);
      client.addChild(request, EORI_VALIDATED, item.getEoriValidated().toString());
    }
    SOAPMessage response = client.executeRequest(request, CLS_NAME_SPACE);
    return unmarshaller.getResponse(response, CLS_RESPONSE_ELEMENT, SAR_XPATH_EXP);
  }

  /**
   * CREATE_SITE_FOR_SAR (CSFS)
   */
  public SpireRefResponse createSiteForSar(SiteItem item) {
    SOAPMessage request = client.initRequest(CSFS_NAME_SPACE, CSFS_REQUEST_CHILD);
    client.addChild(request, VERSION_NO, CSFS_VERSION);
    client.addChild(request, WUA_ID, item.getUserId());
    client.addChild(request, SAR_REF, item.getSarRef());
    client.addChild(request, DIVISION, item.getSiteName());
    client.addChild(request, LITE_ADDRESS, getAddressItemJson(item.getAddressItem()));
    client.addChild(request, ADDRESS, getFriendlyAddress(item.getAddressItem()));
    client.addChild(request, COUNTRY_REF, item.getAddressItem().getCountry());
    SOAPMessage response = client.executeRequest(request, CSFS_NAME_SPACE);
    return unmarshaller.getResponse(response, CSFS_RESPONSE_ELEMENT, SAR_XPATH_EXP);
  }

  /**
   * EDIT_USER_ROLES (EUR)
   */
  public SpireRefResponse editUserRoles(UserRoleItem item) {
    SOAPMessage request = client.initRequest(EUR_NAME_SPACE, EUR_REQUEST_CHILD);
    client.addChild(request, VERSION_NO, EUR_VERSION);
    client.addChild(request, ADMIN_WUA_ID, item.getAdminUserId());
    client.addChild(request, USER_WUA_ID, item.getUserId());
    client.addChild(request, SITE_REF, item.getSiteRef());
    client.addChild(request, ROLE_TYPE, item.getRoleType());
    SOAPMessage response = client.executeRequest(request, EUR_NAME_SPACE);
    return unmarshaller.getResponse(response, EUR_RESPONSE_ELEMENT, SAR_XPATH_EXP);
  }

  protected String getFriendlyAddress(AddressItem item) {
    return Joiner.on("\n").skipNulls()
        .join(item.getLine1(), item.getLine2(), item.getTown(), item.getPostcode(), item.getCounty(), item.getCountry());
  }

  protected String getAddressItemJson(AddressItem item) {
    String json = "";
    try {
      json = mapper.writeValueAsString(item).trim();
    } catch (JsonProcessingException e) {
      LOGGER.error("JsonProcessingException", e);
    }
    return json;
  }

}

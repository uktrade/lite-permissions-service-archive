package uk.gov.bis.lite.spireclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.spireclient.model.SpireAddress;
import uk.gov.bis.lite.spireclient.model.SpireRequest;
import uk.gov.bis.lite.spireclient.model.SpireResponse;
import uk.gov.bis.lite.spireclient.spire.Client;
import uk.gov.bis.lite.spireclient.spire.Unmarshaller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.soap.SOAPMessage;

public class SpireClientService implements SpireClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpireClientService.class);

  private Client client;
  private Unmarshaller unmarshaller;
  private ObjectMapper mapper;
  private List<String> activeEndPoints = new ArrayList<>();

  // Spire Endpoint Version Numbers
  private static final String CREATE_OGEL_APP_VERSION = "1.0";
  private static final String CREATE_LITE_SAR_VERSION = "1.1";
  private static final String CREATE_SITE_FOR_SAR_VERSION = "1.0";
  private static final String EDIT_USER_ROLES_VERSION = "1.1";

  private static final String CONFIG_ERROR = "SpireClientService has not been configured to use this endpoint: ";
  private static final String REQUEST_INVALID = "SpireRequest is not fully populated for endpoint. Invalid";
  private static final String REQUEST_FAILED = "SpireRequest failed to execute";

  /**
   * Spire Endpoints implemented
   */
  public enum Endpoint {
    CREATE_OGEL_APP, CREATE_LITE_SAR, CREATE_SITE_FOR_SAR, EDIT_USER_ROLES;
  }

  public void init(String username, String password, String url, String commaSeparatedEndpoints) {
    this.unmarshaller = new Unmarshaller();
    this.client = new Client(url, username, password);
    this.mapper = new ObjectMapper();
    Collections.addAll(activeEndPoints, commaSeparatedEndpoints.split("\\s*,\\s*"));
    logConfig();
  }

  /**
   * SpireRequest must have Endpoint and userId
   */
  public SpireRequest getSpireRequest(Endpoint endpoint, String userId) {
    SpireRequest request = new SpireRequest();
    request.setUserId(userId);
    request.setEndpoint(endpoint);
    return request;
  }

  /**
   * Attempt to execute request
   */
  public SpireResponse executeRequest(SpireRequest request) {
    Endpoint endpoint = request.getEndpoint();
    if(validRequest(request)) {
      if(activeEndPoints.contains(endpoint.name())) {
        return doExecute(request);
      } else {
        LOGGER.error(CONFIG_ERROR + endpoint.name());
        return SpireResponse.error(CONFIG_ERROR + endpoint.name());
      }
    } else {
      LOGGER.error(REQUEST_INVALID);
      return SpireResponse.error(REQUEST_INVALID);
    }
  }

  private boolean validRequest(SpireRequest request) {
    boolean valid = false;
    Endpoint endpoint = request.getEndpoint();
    // All request require userId
    if (!StringUtils.isBlank(request.getUserId())) {
      if (endpoint.equals(Endpoint.CREATE_LITE_SAR)) { // CHECK: address
        if (request.isAddressValid()) {
          valid = true;
        } else {
          LOGGER.error("CREATE_LITE_SAR request failed validation: address");
        }
      } else if (endpoint.equals(Endpoint.CREATE_SITE_FOR_SAR)) { // CHECK: address, sarRef, siteName
        if (request.isAddressValid() && !StringUtils.isBlank(request.getSarRef())
            && !StringUtils.isBlank(request.getSiteName())) {
          valid = true;
        } else {
          LOGGER.error("CREATE_SITE_FOR_SAR request failed validation: address, sarRef, siteName");
        }
      } else if (endpoint.equals(Endpoint.EDIT_USER_ROLES)) { // CHECK: adminUserId, siteRef, roleType
        if (!StringUtils.isBlank(request.getAdminUserId())
            && !StringUtils.isBlank(request.getSiteRef()) && !StringUtils.isBlank(request.getRoleType())) {
          valid = true;
        } else {
          LOGGER.error("EDIT_USER_ROLES request failed validation: adminUserId, siteRef, roleType");
        }
      } else if (endpoint.equals(Endpoint.CREATE_OGEL_APP)) { // CHECK: sarRef, siteRef, ogelType
        if (!StringUtils.isBlank(request.getSarRef()) && !StringUtils.isBlank(request.getSiteRef())
            && !StringUtils.isBlank(request.getOgelType())) {
          valid = true;
        } else {
          LOGGER.error("CREATE_OGEL_APP request failed validation: sarRef, siteRef, ogelType");
        }
      }
    }
    return valid;
  }

  private SpireResponse doExecute(SpireRequest request) {
    Endpoint endpoint = request.getEndpoint();
    switch (endpoint) {
      case CREATE_SITE_FOR_SAR:
        return doCreateSiteForSar(request);
      case CREATE_LITE_SAR:
        return doCreateLiteSar(request);
      case EDIT_USER_ROLES:
        return doEditUserRoles(request);
      case CREATE_OGEL_APP:
        return doCreateOgelApp(request);
      default:
    }
    return SpireResponse.error(REQUEST_FAILED);
  }

  private SpireResponse doCreateSiteForSar(SpireRequest spireRequest) {
    SOAPMessage request = client.initRequest(SpireNames.CSFS_NAME_SPACE, SpireNames.CSFS_REQUEST_CHILD);
    client.addChild(request, SpireNames.VERSION_NO, CREATE_SITE_FOR_SAR_VERSION);
    client.addChild(request, SpireNames.WUA_ID, spireRequest.getUserId());
    client.addChild(request, SpireNames.SAR_REF, spireRequest.getSarRef());
    client.addChild(request, SpireNames.DIVISION, spireRequest.getSiteName());
    client.addChild(request, SpireNames.LITE_ADDRESS, getAddressItemJson(spireRequest.getAddress()));
    client.addChild(request, SpireNames.ADDRESS, getFriendlyAddress(spireRequest.getAddress()));
    client.addChild(request, SpireNames.COUNTRY_REF, spireRequest.getAddress().getCountry());
    SOAPMessage response = client.executeRequest(request, SpireNames.CSFS_NAME_SPACE);
    return unmarshaller.getSpireResponse(response, SpireNames.CSFS_RESPONSE_ELEMENT, SpireNames.SAR_XPATH_EXP_RESPONSE);
  }

  private SpireResponse doCreateLiteSar(SpireRequest spireRequest) {
    SOAPMessage request = client.initRequest(SpireNames.CLS_NAME_SPACE, SpireNames.CLS_REQUEST_CHILD);
    client.addChild(request, SpireNames.VERSION_NO, CREATE_LITE_SAR_VERSION);
    client.addChild(request, SpireNames.WUA_ID, spireRequest.getUserId());
    client.addChild(request, SpireNames.CUSTOMER_NAME, spireRequest.getCustomerName());
    client.addChild(request, SpireNames.CUSTOMER_TYPE, spireRequest.getCustomerType());
    client.addChild(request, SpireNames.LITE_ADDRESS, getAddressItemJson(spireRequest.getAddress()));
    client.addChild(request, SpireNames.ADDRESS, getFriendlyAddress(spireRequest.getAddress()));
    client.addChild(request, SpireNames.COUNTRY_REF, spireRequest.getAddress().getCountry());
    client.addChild(request, SpireNames.WEBSITE, spireRequest.getWebsite());
    String companiesHouseNumber = spireRequest.getCompaniesHouseNumber();
    if(!StringUtils.isBlank(companiesHouseNumber)) {
      client.addChild(request, SpireNames.COMPANIES_HOUSE_NUMBER, companiesHouseNumber);
      client.addChild(request, SpireNames.COMPANIES_HOUSE_VALIDATED, spireRequest.getCompaniesHouseValidatedStr());
    }
    String eoriNumber = spireRequest.getEoriNumber();
    if(!StringUtils.isBlank(eoriNumber)) {
      client.addChild(request, SpireNames.EORI_NUMBER, eoriNumber);
      client.addChild(request, SpireNames.EORI_VALIDATED, spireRequest.getEoriValidatedStr());
    }
    SOAPMessage response = client.executeRequest(request, SpireNames.CLS_NAME_SPACE);
    return unmarshaller.getSpireResponse(response, SpireNames.CLS_RESPONSE_ELEMENT, SpireNames.SAR_XPATH_EXP_RESPONSE);
  }

  private SpireResponse doEditUserRoles(SpireRequest spireRequest) {
    SOAPMessage request = client.initRequest(SpireNames.EUR_NAME_SPACE, SpireNames.EUR_REQUEST_CHILD);
    client.addChild(request, SpireNames.VERSION_NO, EDIT_USER_ROLES_VERSION);
    client.addChild(request, SpireNames.ADMIN_WUA_ID, spireRequest.getAdminUserId());
    client.addChild(request, SpireNames.USER_WUA_ID, spireRequest.getUserId());
    client.addChild(request, SpireNames.SITE_REF, spireRequest.getSiteRef());
    client.addChild(request, SpireNames.ROLE_TYPE, spireRequest.getRoleType());
    SOAPMessage response = client.executeRequest(request, SpireNames.EUR_NAME_SPACE);
    return unmarshaller.getSpireResponse(response, SpireNames.EUR_RESPONSE_ELEMENT, SpireNames.SAR_XPATH_EXP_RESPONSE);
  }

  private SpireResponse doCreateOgelApp(SpireRequest spireRequest) {
    SOAPMessage request = client.initRequest(SpireNames.COA_NAME_SPACE, SpireNames.COA_REQUEST_CHILD);
    client.addChild(request, SpireNames.VERSION_NO, CREATE_OGEL_APP_VERSION);
    client.addChild(request, SpireNames.WUA_ID, spireRequest.getUserId());
    client.addChild(request, SpireNames.SAR_REF, spireRequest.getSarRef());
    client.addChild(request, SpireNames.SITE_REF, spireRequest.getSiteRef());
    client.addChildList(request, SpireNames.OGL_TYPE_LIST, SpireNames.OGL_TYPE, SpireNames.TYPE, spireRequest.getOgelType());
    SOAPMessage response = client.executeRequest(request, SpireNames.COA_NAME_SPACE);
    return unmarshaller.getSpireResponse(response, SpireNames.COA_RESPONSE_ELEMENT, SpireNames.SAR_XPATH_EXP_RESPONSE);
  }

  private void logConfig() {
    activeEndPoints.forEach(s -> LOGGER.info("Spire Client Config - active endpoint: " + s));
  }

  private String getFriendlyAddress(SpireAddress address) {
    return Joiner.on("\n").skipNulls()
        .join(address.getLine1(), address.getLine2(), address.getTown(),
            address.getPostcode(), address.getCounty(), address.getCountry());
  }

  private String getAddressItemJson(SpireAddress address) {
    String json = "";
    try {
      json = mapper.writeValueAsString(address).trim();
    } catch (JsonProcessingException e) {
      LOGGER.error("JsonProcessingException", e);
    }
    return json;
  }
}

package uk.gov.bis.lite.permissions.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.model.OgelRegistration;
import uk.gov.bis.lite.permissions.model.customer.CustomerItem;
import uk.gov.bis.lite.permissions.model.customer.CustomerResponse;
import uk.gov.bis.lite.permissions.model.customer.UserRoleItem;
import uk.gov.bis.lite.permissions.model.customer.SiteItem;
import uk.gov.bis.lite.permissions.model.request.Address;
import uk.gov.bis.lite.permissions.model.request.AdminApproval;
import uk.gov.bis.lite.permissions.model.request.Customer;
import uk.gov.bis.lite.permissions.model.request.RegisterOgel;
import uk.gov.bis.lite.permissions.model.request.Site;

import java.io.IOException;
import java.util.Optional;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

@Singleton
public class CustomerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

  private final ObjectMapper objectMapper;
  private final String customerServiceUrl;
  private final String customerPath;
  private final String sitePath;
  private final String userRolePath;
  private final Client httpClient;

  @Inject
  public CustomerService(Client httpClient,
                         @Named("customerServiceUrl") String customerServiceUrl,
                         @Named("customerServiceCustomerPath") String customerPath,
                         @Named("customerServiceSitePath") String sitePath,
                         @Named("customerServiceUserRolePath") String userRolePath) {
    this.objectMapper = new ObjectMapper();
    this.httpClient = httpClient;
    this.customerServiceUrl = customerServiceUrl;
    this.customerPath = customerPath;
    this.sitePath = sitePath;
    this.userRolePath = userRolePath;
  }

  public Optional<String> updateUserRole(OgelRegistration registration) {
    WebTarget target = httpClient.target(customerServiceUrl).path(userRolePath);
    try {
      Response response = target.request().post(Entity.json(getUserRoleItem(registration)));
      if (isOk(response)) {
        CustomerResponse customerResponse = objectMapper.readValue(response.readEntity(String.class), CustomerResponse.class);
        LOGGER.info("CustomerResponse siteAccess: " + customerResponse.getResponse());
        return Optional.of(customerResponse.getResponse());
      } else {
        LOGGER.warn("Response not ok: " + response.getStatus());
      }
    } catch (IOException e) {
      LOGGER.error("IOException", e);
    }
    return Optional.empty();
  }

  public Optional<String> createCustomer(OgelRegistration registration) {
    WebTarget target = httpClient.target(customerServiceUrl).path(customerPath);
    try {
      Response response = target.request().post(Entity.json(getCustomerItem(registration)));
      if (isOk(response)) {
        CustomerResponse customerResponse = objectMapper.readValue(response.readEntity(String.class), CustomerResponse.class);
        LOGGER.info("CustomerResponse SarRef: " + customerResponse.getResponse());
        return Optional.of(customerResponse.getResponse());
      } else {
        LOGGER.warn("Response not ok: " + response.getStatus());
      }
    } catch (IOException | ProcessingException e) {
      LOGGER.error("Exception: " + e);
    }
    return Optional.empty();
  }

  public Optional<String> createSite(OgelRegistration registration) {
    WebTarget target = httpClient.target(customerServiceUrl).path(sitePath);
    try {
      Response response = target.request().post(Entity.json(getSiteItem(registration)));
      if (isOk(response)) {
        CustomerResponse customerResponse = objectMapper.readValue(response.readEntity(String.class), CustomerResponse.class);
        LOGGER.info("CustomerResponse SiteRef: " + customerResponse.getResponse());
        return Optional.of(customerResponse.getResponse());
      } else {
        LOGGER.warn("Response not ok: " + response.getStatus());
      }
    } catch (IOException | ProcessingException e) {
      LOGGER.error("Exception", e);
    }
    return Optional.empty();
  }

  private CustomerItem getCustomerItem(OgelRegistration ogel) {
    RegisterOgel reg = ogel.getRegisterOgelFromJson();
    Customer customer = reg.getNewCustomer();
    Address address = customer.getRegisteredAddress();
    CustomerItem item = new CustomerItem();
    item.setUserId(ogel.getUserId());
    item.setCustomerName(customer.getCustomerName());
    item.setAddress(address.getSpireAddress());
    item.setCompaniesHouseNumber(customer.getChNumber());
    item.setCompaniesHouseValidated(customer.isChNumberValidated());
    item.setCustomerType(customer.getCustomerType());
    item.setEoriNumber(customer.getEoriNumber());
    item.setEoriValidated(customer.isEoriNumberValidated());
    item.setLiteAddress(address.getLiteAddress());
    item.setCountryRef(address.getCountry());
    item.setWebsite(customer.getWebsite());
    return item;
  }

  private SiteItem getSiteItem(OgelRegistration ogel) {
    RegisterOgel reg = ogel.getRegisterOgelFromJson();
    Customer customer = reg.getNewCustomer();
    Site site = reg.getNewSite();
    Address address = customer.getRegisteredAddress();
    SiteItem item = new SiteItem();
    item.setUserId(ogel.getUserId());
    item.setSarRef(ogel.getCustomerId());
    item.setAddress(address.getSpireAddress());
    item.setLiteAddress(address.getLiteAddress());
    item.setDivision(site.getSiteName());
    item.setCountryRef(address.getCountry());
    return item;
  }

  private UserRoleItem getUserRoleItem(OgelRegistration ogel) {
    RegisterOgel reg = ogel.getRegisterOgelFromJson();
    AdminApproval admin = reg.getAdminApproval();
    UserRoleItem item = new UserRoleItem();
    item.setUserId(ogel.getUserId());
    item.setAdminUserId(admin.getAdminUserId());
    item.setSiteRef(ogel.getSiteId());
    return item;
  }

  private boolean isOk(Response response) {
    return response != null && (response.getStatus() == Response.Status.OK.getStatusCode());
  }
}

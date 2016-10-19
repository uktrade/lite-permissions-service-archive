package uk.gov.bis.lite.permissions.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.model.customer.AddressItem;
import uk.gov.bis.lite.permissions.model.customer.CustomerItem;
import uk.gov.bis.lite.permissions.model.customer.CustomerResponse;
import uk.gov.bis.lite.permissions.model.customer.SiteItem;
import uk.gov.bis.lite.permissions.model.customer.UserRoleItem;
import uk.gov.bis.lite.permissions.model.register.Address;
import uk.gov.bis.lite.permissions.model.register.AdminApproval;
import uk.gov.bis.lite.permissions.model.register.Customer;
import uk.gov.bis.lite.permissions.model.register.RegisterOgel;
import uk.gov.bis.lite.permissions.model.register.Site;

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

  private static final String DEFAULT_SITE_NAME = "Main Site";

  private static final String ROLE_TYPE_ADMIN = "ADMIN";
  private static final String ROLE_TYPE_SUBMITTER = "SUBMITTER";
  private static final String ROLE_TYPE_PREPARER = "PREPARER";

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

  public Optional<String> updateUserRole(OgelSubmission sub) {
    WebTarget target = httpClient.target(customerServiceUrl).path(userRolePath);
    try {
      Response response = target.request().post(Entity.json(getUserRoleItem(sub)));
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

  public Optional<String> createCustomer(OgelSubmission sub) {
    WebTarget target = httpClient.target(customerServiceUrl).path(customerPath);
    try {
      Response response = target.request().post(Entity.json(getCustomerItem(sub)));
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

  public Optional<String> createSite(OgelSubmission sub) {
    WebTarget target = httpClient.target(customerServiceUrl).path(sitePath);
    try {
      Response response = target.request().post(Entity.json(getSiteItem(sub)));
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

  private CustomerItem getCustomerItem(OgelSubmission sub) {
    RegisterOgel reg = sub.getRegisterOgelFromJson();
    Customer customer = reg.getNewCustomer();
    Address address = customer.getRegisteredAddress();
    CustomerItem item = new CustomerItem();
    item.setUserId(sub.getUserId());
    item.setCustomerName(customer.getCustomerName());
    item.setAddressItem(getAddressItem(address));
    item.setCompaniesHouseNumber(customer.getChNumber());
    item.setCompaniesHouseValidated(customer.isChNumberValidated());
    item.setCustomerType(customer.getCustomerType());
    item.setEoriNumber(customer.getEoriNumber());
    item.setEoriValidated(customer.isEoriNumberValidated());
    item.setWebsite(customer.getWebsite());
    return item;
  }

  private AddressItem getAddressItem(Address address) {
    AddressItem item = new AddressItem();
    item.init(address);
    return item;
  }

  private SiteItem getSiteItem(OgelSubmission sub) {
    RegisterOgel reg = sub.getRegisterOgelFromJson();
    Customer customer = reg.getNewCustomer();
    Site site = reg.getNewSite();

    Address address = site.isUseCustomerAddress() ? customer.getRegisteredAddress() : site.getAddress();
    String siteName = site.getSiteName() != null ? site.getSiteName() : DEFAULT_SITE_NAME;

    SiteItem item = new SiteItem();
    item.setSiteName(siteName);
    item.setUserId(sub.getUserId());
    item.setSarRef(sub.getCustomerRef());
    item.setAddressItem(getAddressItem(address));
    return item;
  }

  /**
   * Creates a UserRoleItem with an ADMIN roleType
   */
  private UserRoleItem getUserRoleItem(OgelSubmission sub) {
    RegisterOgel reg = sub.getRegisterOgelFromJson();
    AdminApproval admin = reg.getAdminApproval();
    UserRoleItem item = new UserRoleItem();
    item.setRoleType(ROLE_TYPE_ADMIN); // hardcoded for now
    item.setUserId(sub.getUserId());
    item.setAdminUserId(admin.getAdminUserId());
    item.setSiteRef(sub.getSiteRef());
    return item;
  }

  private boolean isOk(Response response) {
    return response != null && (response.getStatus() == Response.Status.OK.getStatusCode());
  }
}

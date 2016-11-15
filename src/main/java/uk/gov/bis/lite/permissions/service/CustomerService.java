package uk.gov.bis.lite.permissions.service;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.customer.api.item.AddressItem;
import uk.gov.bis.lite.customer.api.item.in.CustomerIn;
import uk.gov.bis.lite.customer.api.item.in.SiteIn;
import uk.gov.bis.lite.customer.api.item.in.UserRoleIn;
import uk.gov.bis.lite.customer.api.item.out.CustomerOut;
import uk.gov.bis.lite.customer.api.item.out.CustomerServiceOut;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.model.register.Address;
import uk.gov.bis.lite.permissions.model.register.AdminApproval;
import uk.gov.bis.lite.permissions.model.register.Customer;
import uk.gov.bis.lite.permissions.model.register.RegisterOgel;
import uk.gov.bis.lite.permissions.model.register.Site;

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

  private FailService failService;
  private String customerServiceUrl;
  private Client httpClient;

  @Inject
  public CustomerService(Client httpClient, FailService failService,
                         @Named("customerServiceUrl") String customerServiceUrl) {
    this.httpClient = httpClient;
    this.failService = failService;
    this.customerServiceUrl = customerServiceUrl;
  }

  /**
   * Get Customer using  companyNumber OR Use CustomerService to create Customer
   * Returns sarRef if successful
   * Notifies FailService if there is an error during create process
   */
  Optional<String> getOrCreateCustomer(OgelSubmission sub) {
    // We first attempt to get Customer using the companyNumber
    String companyNumber = getCustomerItem(sub).getCompaniesHouseNumber();
    if (!StringUtils.isBlank(companyNumber)) {
      Optional<String> customerId = getCustomerIdByCompanyNumber(companyNumber);
      if (customerId.isPresent()) {
        return customerId;
      }
    }
    // No Customer exists for companyNumber, so we attempt to create a new one
    return createCustomer(sub);
  }

  /**
   * Uses CustomerService to create Site
   * Returns siteRef if successful, notifies FailService if there is an error
   */
  Optional<String> createSite(OgelSubmission sub) {

    String createSitePath = "/customer-sites/{customerId}";
    String path = createSitePath.replace("{customerId}", sub.getCustomerRef());

    WebTarget target = httpClient.target(customerServiceUrl).queryParam("userId", sub.getUserId()).path(path);
    try {
      Response response = target.request().post(Entity.json(getSiteItemIn(sub)));
      if (isOk(response)) {
        return Optional.of(response.readEntity(CustomerServiceOut.class).getResponse());
      } else {
        failService.fail(sub, response, FailService.Origin.SITE);
      }
    } catch (ProcessingException e) {
      failService.fail(sub, e, FailService.Origin.SITE);
    }
    return Optional.empty();
  }

  /**
   * Uses CustomerService to update a UserRole
   * Returns 'COMPLETE' if successful, notifies FailService if there is an error
   */
  Optional<String> updateUserRole(OgelSubmission sub) {

    String userRolePath = "/user-roles/user/{userId}/site/{siteRef}";
    String path = userRolePath.replace("{userId}", sub.getUserId());
    path = path.replace("{siteRef}", sub.getSiteRef());

    WebTarget target = httpClient.target(customerServiceUrl).path(path);
    Response response = target.request().post(Entity.json(getUserRoleItem(sub)));
    if (isOk(response)) {
      return Optional.of(response.readEntity(CustomerServiceOut.class).getResponse());
    } else {
      failService.fail(sub, response, FailService.Origin.USER_ROLE);
    }
    return Optional.empty();
  }

  /**
   * Uses CustomerService to create Customer
   * Returns sarRef if successful, notifies FailService if there is an error
   */
  private Optional<String> createCustomer(OgelSubmission sub) {

    WebTarget target = httpClient.target(customerServiceUrl).path("/create-customer");
    try {
      Response response = target.request().post(Entity.json(getCustomerItem(sub)));
      if (isOk(response)) {
        return Optional.of(response.readEntity(CustomerServiceOut.class).getResponse());
      } else {
        failService.fail(sub, response, FailService.Origin.CUSTOMER);
      }
    } catch (ProcessingException e) {
      failService.fail(sub, e, FailService.Origin.CUSTOMER);
    }
    return Optional.empty();
  }

  /**
   * Uses CustomerService to get CustomerId from the companyNumber
   */
  private Optional<String> getCustomerIdByCompanyNumber(String companyNumber) {
    String customerSearchByCompanyNumberPath = "/search-customers/registered-number/{chNumber}";
    WebTarget target = httpClient.target(customerServiceUrl)
        .path(customerSearchByCompanyNumberPath.replace("{chNumber}", companyNumber));
    try {
      Response response = target.request().get();
      if (isOk(response)) {
        CustomerOut customer =
            response.readEntity(CustomerOut.class);
        return Optional.of(customer.getSarRef());
      }
    } catch (ProcessingException e) {
      LOGGER.warn("Exception getCustomerIdByCompanyNumber: " + Throwables.getStackTraceAsString(e));
    }
    return Optional.empty();
  }

  private CustomerIn getCustomerItem(OgelSubmission sub) {
    RegisterOgel reg = sub.getRegisterOgelFromJson();
    Customer customer = reg.getNewCustomer();
    Address address = customer.getRegisteredAddress();
    CustomerIn item = new CustomerIn();
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
    item.setLine1(address.getLine1());
    item.setLine2(address.getLine2());
    item.setTown(address.getTown());
    item.setCounty(address.getCounty());
    item.setPostcode(address.getPostcode());
    item.setCountry(address.getCountry());
    return item;
  }

  private SiteIn getSiteItemIn(OgelSubmission sub) {
    RegisterOgel reg = sub.getRegisterOgelFromJson();
    Site site = reg.getNewSite();
    String siteName = site.getSiteName() != null ? site.getSiteName() : DEFAULT_SITE_NAME;
    Address address = site.isUseCustomerAddress() ? reg.getNewCustomer().getRegisteredAddress() : site.getAddress();

    SiteIn siteIn = new SiteIn();
    siteIn.setSiteName(siteName);
    siteIn.setAddress(getAddressItem(address));
    return siteIn;
  }

  /**
   * Creates a UserRoleItem with an ADMIN roleType
   */
  private UserRoleIn getUserRoleItem(OgelSubmission sub) {
    RegisterOgel reg = sub.getRegisterOgelFromJson();
    AdminApproval admin = reg.getAdminApproval();
    UserRoleIn item = new UserRoleIn();
    item.setRoleType(UserRoleIn.RoleType.ADMIN);
    item.setAdminUserId(admin.getAdminUserId());
    return item;
  }

  private boolean isOk(Response response) {
    return response != null && (response.getStatus() == Response.Status.OK.getStatusCode());
  }

}

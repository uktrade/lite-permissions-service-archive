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
import uk.gov.bis.lite.permissions.model.customer.SiteItem;
import uk.gov.bis.lite.permissions.model.request.Address;
import uk.gov.bis.lite.permissions.model.request.Customer;
import uk.gov.bis.lite.permissions.model.request.RegisterOgel;
import uk.gov.bis.lite.permissions.model.request.Site;

import java.io.IOException;
import java.util.Optional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

@Singleton
public class CustomerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

  private final ObjectMapper objectMapper;

  private final String customerUrl;
  private final String customerPath;

  private final String sitePath;

  private final Client httpClient;

  @Inject
  public CustomerService(Client httpClient,
                         @Named("customerServiceHost") String host,
                         @Named("customerServiceCustomerPath") String customerPath,
                         @Named("customerServiceSitePath") String sitePath,
                         @Named("customerServicePort") String port) {
    this.objectMapper = new ObjectMapper();
    this.httpClient = httpClient;
    this.customerUrl = "http://" + host + ":" + port;
    this.customerPath = customerPath;
    this.sitePath = sitePath;
  }

  public Optional<String> createCustomer(OgelRegistration registration) {
    WebTarget target = httpClient.target(customerUrl).path(customerPath);
    try {
      Response response = target.request().post(Entity.json(getCustomerItem(registration)));
      if (isOk(response)) {
        CustomerResponse customerResponse = objectMapper.readValue(response.readEntity(String.class), CustomerResponse.class);
        LOGGER.info("CustomerResponse SarRef: " + customerResponse.getSarRef());
        return Optional.of(customerResponse.getSarRef());
      }
    } catch (IOException e) {
      LOGGER.error("IOException", e);
    }
    return Optional.empty();
  }

  public Optional<String> createSite(OgelRegistration registration) {
    WebTarget target = httpClient.target(customerUrl).path(sitePath);
    try {
      Response response = target.request().post(Entity.json(getSiteItem(registration)));
      if (isOk(response)) {
        CustomerResponse customerResponse = objectMapper.readValue(response.readEntity(String.class), CustomerResponse.class);
        LOGGER.info("CustomerResponse SiteRef: " + customerResponse.getSiteRef());
        return Optional.of(customerResponse.getSiteRef());
      }
    } catch (IOException e) {
      LOGGER.error("IOException", e);
    }
    return Optional.empty();
  }

  private CustomerItem getCustomerItem(OgelRegistration registration) {
    RegisterOgel regOgel = registration.getRegisterOgelFromJson();
    Customer customer = regOgel.getNewCustomer();
    Address address = customer.getRegisteredAddress();
    CustomerItem item = new CustomerItem();
    item.setUserId(registration.getUserId());
    item.setCustomerName(registration.getCustomerId());
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

  private SiteItem getSiteItem(OgelRegistration registration) {
    RegisterOgel regOgel = registration.getRegisterOgelFromJson();
    Customer customer = regOgel.getNewCustomer();
    Site site = regOgel.getNewSite();
    Address address = customer.getRegisteredAddress();
    SiteItem item = new SiteItem();
    item.setUserId(registration.getUserId());
    item.setSarRef(registration.getCustomerId());
    item.setAddress(address.getSpireAddress());
    item.setLiteAddress(address.getLiteAddress());
    item.setDivision(site.getSiteName());
    item.setCountryRef(address.getCountry());
    return item;
  }

  private boolean isOk(Response response) {
    return response != null && (response.getStatus() == Response.Status.OK.getStatusCode());
  }
}

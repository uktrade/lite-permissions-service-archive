package uk.gov.bis.lite.permissions.pact;


import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.CustomerServiceImpl;

import javax.ws.rs.client.ClientBuilder;

public class CustomerPactTest {

  private CustomerService customerService;

  // Customer
  private static String customerViewResponse = fixture("fixture/pact/customerViewResponse.json");
  private static String customerParamSuccess = fixture("fixture/pact/customerParamSuccess.json");
  private static String customerParamMissingUserId = fixture("fixture/pact/customerParamMissingUserId.json");

  // Site
  private static String siteViewResponse = fixture("fixture/pact/siteViewResponse.json");
  private static String siteParamSuccess = fixture("fixture/pact/siteParamSuccess.json");

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("lite-customer-service", this);

  @Before
  public void before() {
    customerService = new CustomerServiceImpl(ClientBuilder.newClient(), mockProvider.getConfig().url());
  }

  @Pact(provider = "lite-customer-service", consumer = "lite-permissions-service")
  public PactFragment createFragment(PactDslWithProvider builder) {

    return builder
        .uponReceiving("CreateCustomer Success")
        .path("/create-customer")
        .method("POST")
        .body(customerParamSuccess)
        .willRespondWith()
        .status(200)
        .body(customerViewResponse, ContentType.APPLICATION_JSON)
        .uponReceiving("CreateCustomer Missing UserId")
        .path("/create-customer")
        .method("POST")
        .body(customerParamMissingUserId)
        .willRespondWith()
        .status(400)
        .uponReceiving("CreateSite Success")
        .path("/customer-sites/CUSTOMER_ID")
        .method("POST")
        .query("userId=userId")
        .body(siteParamSuccess)
        .willRespondWith()
        .status(200)
        .body(siteViewResponse, ContentType.APPLICATION_JSON)
        .toFragment();
  }

  @Test
  @PactVerification("lite-customer-service")
  public void testCustomerServicePact() throws Exception {

    // Test Customer Success
    OgelSubmission subCustomerSuccess = getCustomerOgelSubmission(fixture("fixture/pact/registerParam.json"));
    assertThat(customerService.createCustomer(subCustomerSuccess)).isPresent();

    // Test Customer Missing UserId

    OgelSubmission subMissingUserId = getCustomerOgelSubmission(fixture("fixture/pact/registerParam.json"));
    subMissingUserId.setUserId("");
    assertThat(customerService.createCustomer(subMissingUserId)).isNotPresent();

    // Test Site Success
    OgelSubmission subSiteSuccess = getCustomerOgelSubmission(fixture("fixture/pact/registerParam.json"));
    subSiteSuccess.setCustomerRef("CUSTOMER_ID");
    assertThat(customerService.createSite(subSiteSuccess)).isPresent();

  }

  private OgelSubmission getCustomerOgelSubmission(String json) {
    OgelSubmission sub = new OgelSubmission("userId", "ogelType");
    sub.setJson(json);
    return sub;
  }
}

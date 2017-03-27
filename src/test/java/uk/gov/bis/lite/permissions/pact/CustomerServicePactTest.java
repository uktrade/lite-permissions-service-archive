package uk.gov.bis.lite.permissions.pact;


import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.CustomerServiceImpl;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.ClientBuilder;

public class CustomerServicePactTest {

  private CustomerService customerService;

  // Customer
  private static String mockCustomerView = fixture("fixture/pact/mockCustomerView.json");
  private static String mockCustomerParam = fixture("fixture/pact/mockCustomerParam.json");
  private static String mockCustomerParamNoUserId = fixture("fixture/pact/mockCustomerParamNoUserId.json");

  // Site
  private static String mockSiteView = fixture("fixture/pact/mockSiteView.json");
  private static String mockSiteParam = fixture("fixture/pact/mockSiteParam.json");

  // UserRole
  private static String mockUserRoleUpdate = fixture("fixture/pact/mockUserRoleUpdate.json");

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("lite-customer-service", this);

  @Before
  public void before() {
    customerService = new CustomerServiceImpl(ClientBuilder.newClient(), mockProvider.getConfig().url());
  }

  @Pact(provider = "lite-customer-service", consumer = "lite-permissions-service")
  public PactFragment createFragment(PactDslWithProvider builder) {

    return builder
        .uponReceiving("Get Customer By Company Number")
          .path("/search-customers/registered-number/COMPANY_NUMBER")
          .method("GET")
            .willRespondWith()
          .status(200)
          .headers(headers())
          .body(mockCustomerView)
        .uponReceiving("Create Customer Success")
          .path("/create-customer")
          .headers(headers())
          .method("POST")
          .body(mockCustomerParam)
            .willRespondWith()
              .headers(headers())
              .status(200)
              .body(mockCustomerView)
        .uponReceiving("Create Customer No UserId")
          .path("/create-customer")
          .headers(headers())
          .method("POST")
          .body(mockCustomerParamNoUserId)
            .willRespondWith()
              .status(400)
        .uponReceiving("Create Site Success")
          .path("/customer-sites/CUSTOMER_ID")
          .headers(headers())
          .method("POST")
          .query("userId=userId")
          .body(mockSiteParam)
            .willRespondWith()
              .headers(headers())
              .status(200)
              .body(mockSiteView)
        .uponReceiving("Update UserRole")
          .path("/user-roles/user/USER_ID/site/SITE_REF")
          .headers(headers())
          .method("POST")
          .body(mockUserRoleUpdate)
            .willRespondWith()
              .status(200)
        .toFragment();
  }

  @Test
  @PactVerification("lite-customer-service")
  public void testCustomerServicePact() throws Exception {

    // Create Customer Success
    OgelSubmission sub1 = getMockedOgelSubmission();
    assertThat(customerService.createCustomer(sub1)).isPresent();

    // Create Customer No UserId
    OgelSubmission sub2 = getMockedOgelSubmission();
    sub2.setUserId("");
    assertThat(customerService.createCustomer(sub2)).isNotPresent();

    // Create Site
    OgelSubmission sub3 = getMockedOgelSubmission();
    sub3.setCustomerRef("CUSTOMER_ID");
    assertThat(customerService.createSite(sub3)).isPresent();

    // Get Customer By Company Number
    assertThat(customerService.getCustomerIdByCompanyNumber("COMPANY_NUMBER")).isPresent();

    // Update UserRole
    OgelSubmission sub4 = getCustomerOgelSubmission(fixture("fixture/pact/mockRegisterParamSite.json"));
    sub4.setAdminUserId("ADMIN_USER_ID");
    sub4.setSiteRef("SITE_REF");
    sub4.setUserId("USER_ID");
    assertThat(customerService.updateUserRole(sub4)).isTrue();
  }

  private OgelSubmission getMockedOgelSubmission() {
    OgelSubmission sub = new OgelSubmission("userId", "ogelType");
    sub.setJson(fixture("fixture/pact/mockRegisterParam.json"));
    return sub;
  }

  private OgelSubmission getCustomerOgelSubmission(String json) {
    OgelSubmission sub = new OgelSubmission("userId", "ogelType");
    sub.setJson(json);
    return sub;
  }

  private Map<String, String> headers() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    return headers;
  }

}

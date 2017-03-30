package uk.gov.bis.lite.permissions.pact;


import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.CustomerServiceImpl;

import javax.ws.rs.client.ClientBuilder;

/**
 * CustomerCustomerPactTest - creates Pact file: lie-permissions-service-lite-customer-service.json
 */
public class CustomerCustomerPactTest extends CustomerBasePactTest {

  private CustomerService customerService;

  private static final String COMPANY_NUMBER_SUCCESS = "COMPANY_NUMBER_SUCCESS";
  private static final String COMPANY_NUMBER_FAIL = "COMPANY_NUMBER_FAIL";

  // Customer Fixtures
  // Currently cannot use DSL because it fails to match properly for userId (with and without)
  private static String mockCustomerParam = fixture("fixture/pact/customerParam.json");
  private static String mockCustomerParamNoUserId = fixture("fixture/pact/customerParamNoUserId.json");

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PROVIDER, this);

  @Before
  public void before() {
    customerService = new CustomerServiceImpl(ClientBuilder.newClient(), mockProvider.getConfig().url());
  }

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment createFragment(PactDslWithProvider builder) {

    return builder
        .given("create customer success")
        .uponReceiving("create customer success")
          .path("/create-customer")
          .headers(headers())
          .method("POST")
          .body(mockCustomerParam)
          .willRespondWith()
            .headers(headers())
            .status(200)
            .body(customerView())
        .given("create customer fail")
        .uponReceiving("create customer fail")
          .path("/create-customer")
          .headers(headers())
          .method("POST")
          .body(mockCustomerParamNoUserId)
          .willRespondWith()
            .status(400)
        .given("customer by company number success")
        .uponReceiving("customer by company number success")
          .path("/search-customers/registered-number/" + COMPANY_NUMBER_SUCCESS)
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers())
            .body(customerView())
        .given("customer by company number fail")
        .uponReceiving("customer by company number fail")
          .path("/search-customers/registered-number/" + COMPANY_NUMBER_FAIL)
          .method("GET")
          .willRespondWith()
            .status(404)
        .toFragment();
  }

  @Test
  @PactVerification(PROVIDER)
  public void testCustomerServicePact() throws Exception {

    // Create Customer Success
    OgelSubmission subSuccess = getOgelSubmission(getRegisterParam(fixture(FIXTURE_REGISTER_PARAM_NEW)));
    assertThat(customerService.createCustomer(subSuccess)).isPresent();

    // Create Customer Fail (no userId)
    OgelSubmission subFail = getOgelSubmission(getRegisterParam(fixture(FIXTURE_REGISTER_PARAM_NEW)));
    subFail.setUserId("");
    assertThat(customerService.createCustomer(subFail)).isNotPresent();

    // Customer by CompanyNumber Success
    assertThat(customerService.getCustomerIdByCompanyNumber(COMPANY_NUMBER_SUCCESS)).isPresent();

    // Customer by CompanyNumber NoFound
    assertThat(customerService.getCustomerIdByCompanyNumber(COMPANY_NUMBER_FAIL)).isNotPresent();

  }

  private PactDslJsonBody customerView() {
    return new PactDslJsonBody()
        .stringType("customerId")
        .stringType("companyName")
        .stringType("companyNumber")
        .stringType("shortName")
        .stringType("organisationType")
        .stringType("registrationStatus")
        .stringType("registeredAddress")
        .stringType("applicantType")
        .stringType("countryOfOriginCode")
        .array("websites").closeArray()
        .asBody();
  }
}

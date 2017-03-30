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
 * CustomerSitePactTest
 */
public class CustomerSitePactTest extends CustomerBasePactTest {

  private CustomerService customerService;

  private static final String CUSTOMER_ID = "CUSTOMER_ID";
  private final static String PROVIDER = "lite-customer-service";
  private final static String CONSUMER = "lite-permissions-service";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PROVIDER, this);

  @Before
  public void before() {
    customerService = new CustomerServiceImpl(ClientBuilder.newClient(), mockProvider.getConfig().url());
  }

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment createFragment(PactDslWithProvider builder) {

    return builder
        .given("create site success")
        .uponReceiving("create site success")
          .path("/customer-sites/" + CUSTOMER_ID)
          .headers(headers())
          .method("POST")
          .query("userId=userId")
          .body(siteParam())
            .willRespondWith()
              .headers(headers())
              .status(200)
              .body(siteView())
        .given("create site fail")
        .uponReceiving("create site fail")
          .path("/customer-sites/" + CUSTOMER_ID)
          .headers(headers())
          .method("POST")
          .query("userId=")
          .body(siteParam())
            .willRespondWith()
            .status(400)
        .toFragment();
  }

  @Test
  @PactVerification(PROVIDER)
  public void testCustomerServicePact() throws Exception {

    // Create Site Success
    OgelSubmission subSuccess = getOgelSubmission(getRegisterParam(fixture(FIXTURE_REGISTER_PARAM_NEW)));
    subSuccess.setCustomerRef(CUSTOMER_ID);
    assertThat(customerService.createSite(subSuccess)).isPresent();

    // Create Site Fail (no userId)
    OgelSubmission subFail = getOgelSubmission(getRegisterParam(fixture(FIXTURE_REGISTER_PARAM_NEW)));
    subFail.setCustomerRef(CUSTOMER_ID);
    subFail.setUserId("");
    assertThat(customerService.createSite(subFail)).isNotPresent();

  }

  private PactDslJsonBody siteParam() {
    return new PactDslJsonBody()
        .stringType("siteName")
        .object("addressParam")
        .stringType("line1")
        .stringType("line2")
        .stringType("town")
        .stringType("county")
        .stringType("postcode")
        .stringType("country")
        .closeObject()
        .asBody();
  }

  private PactDslJsonBody siteView() {
    return new PactDslJsonBody()
        .stringType("siteId")
        .stringType("customerId")
        .stringType("siteName")
        .object("address")
        .stringType("plainText")
        .stringType("country")
        .closeObject()
        .asBody();
  }
}
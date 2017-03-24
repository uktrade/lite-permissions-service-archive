package uk.gov.bis.lite.permissions.pactx;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.CustomerServiceImpl;
import uk.gov.bis.lite.permissions.service.CustomerServiceTest;


public class CreateCustomerPactTest {

  private static String customerView = fixture("fixture/pact/customerView.json");
  private static String customerParam = fixture("fixture/pact/customerParam.json");

  private static String url = "http://localhost:8080";
  private static String path = "/create-customer";

  private CustomerService customerService;

  private static String CUSTOMER_ID = "CUSTOMER_ID";
  private static String SITE_ID = "SITE_ID";

  private static String SUCCESS = "SUCCESS";
  private static String FORBIDDEN = "FORBIDDEN";
  private static String BAD_REQUEST = "BAD_REQUEST";
  private static String createSiteMode = SUCCESS;
  private static String createCustomerMode = SUCCESS;
  private static String updateUserRoleMode = SUCCESS;

  @Rule
  public PactProviderRule provider = new PactProviderRule("customer-create-customer", "localhost", 8080, this);

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new CustomerServiceTest.CreateCustomerEndpoint()).build();


  @Before
  public void before() {
    customerService = new CustomerServiceImpl(resources.client(), "/");
  }

  @Pact(provider="customer-create-customer", consumer="permissions")
  public PactFragment createFragment(PactDslWithProvider builder) {
    return builder
        .uponReceiving("CreateCustomer test interaction")
        .path(path)
        .method("POST")
        .body(customerParam)
        .willRespondWith()
        .status(200)
        .body(customerView)
        .toFragment();
  }

  @Test
  @PactVerification("customer-create-customer")
  public void testCustomerSuccess() throws Exception {
    // Setup
    customerMode(SUCCESS);

    OgelSubmission sub = getCustomerOgelSubmission();

    // Test
    assertThat(customerService.getOrCreateCustomer(sub)).isPresent().contains(CUSTOMER_ID);
    assertThat(sub.hasFailEvent()).isFalse();
  }

  //@Test
  //@PactVerification("customer-create-customer")
  //public void runTest() throws IOException {
  //  assertEquals(new ConsumerClient(url).postBody(path, customerParam, ContentType.APPLICATION_JSON), customerView);
  //}

  private void customerMode(String arg) {
    createCustomerMode = arg;
  }

  private OgelSubmission getCustomerOgelSubmission() {
    String newSiteJson = fixture("fixture/createCustomer.json");
    OgelSubmission sub = new OgelSubmission("userId", "ogelType");
    sub.setJson(newSiteJson);
    return sub;
  }
}
package uk.gov.bis.lite.permissions.pactx;

import static groovy.util.GroovyTestCase.assertEquals;
import static io.dropwizard.testing.FixtureHelpers.fixture;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;


public class CustomerByCompanyNumberPactTest {

  private static String customerView = fixture("fixture/pact/customerView.json");
  private static String url = "http://localhost:8080";
  private static String path = "/search-customers/registered-number/{chNumber}";

  @Rule
  public PactProviderRule provider = new PactProviderRule("customer-customer-company-number", "localhost", 8080, this);

  @Pact(provider="customer-customer-company-number", consumer="permissions")
  public PactFragment createFragment(PactDslWithProvider builder) {
    return builder
        .uponReceiving("CustomerByCompanyNumberPactTest test interaction")
        .path(path)
        .method("GET")
        .willRespondWith()
        .status(200)
        .body(customerView)
        .toFragment();
  }

  @Test
  @PactVerification("customer-customer-company-number")
  public void runTest() throws IOException {
    assertEquals(new ConsumerClient(url).getAsJsonString(path), customerView);
  }
}

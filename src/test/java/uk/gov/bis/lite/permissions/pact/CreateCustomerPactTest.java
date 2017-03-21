package uk.gov.bis.lite.permissions.pact;

import static groovy.util.GroovyTestCase.assertEquals;
import static io.dropwizard.testing.FixtureHelpers.fixture;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.apache.http.entity.ContentType;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;


public class CreateCustomerPactTest {

  private static String customerView = fixture("fixture/pact/customerView.json");
  private static String url = "http://localhost:8080";
  private static String path = "/create-customer";

  @Rule
  public PactProviderRule provider = new PactProviderRule("customerServiceCreateCustomerProvider", "localhost", 8080, this);

  @Pact(provider="customerServiceCreateCustomerProvider", consumer="permissionsServiceCreateCustomerConsumer")
  public PactFragment createFragment(PactDslWithProvider builder) {
    return builder
        .uponReceiving("CreateCustomer test interaction")
        .path(path)
        .method("POST")
        .willRespondWith()
        .status(200)
        .body(customerView)
        .toFragment();
  }

  @Test
  @PactVerification("customerServiceCreateCustomerProvider")
  public void runTest() throws IOException {
    assertEquals(new ConsumerClient(url).postBody(path, fixture("fixture/pact/customerParam.json"), ContentType.APPLICATION_JSON), customerView);
  }
}
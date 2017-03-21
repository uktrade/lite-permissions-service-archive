package uk.gov.bis.lite.permissions.pact;

import static io.dropwizard.testing.FixtureHelpers.fixture;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;


public class CreateSitePactTest {

  private static String siteParam = fixture("fixture/pact/siteParam.json");
  private static String url = "http://localhost:8080";
  private static String path = "/customer-sites/{customerId}";

  @Rule
  public PactProviderRule provider = new PactProviderRule("customerServiceCreateSiteProvider", "localhost", 8080, this);

  @Pact(provider="customerServiceCreateSiteProvider", consumer="permissionsServiceCreateSiteConsumer")
  public PactFragment createFragment(PactDslWithProvider builder) {
    return builder
        .given("test state")
        .uponReceiving("CreateSitePactTest test interaction")
        .path(path)
        .method("POST")
        .willRespondWith()
        .status(200)
        .body(siteParam)
        .toFragment();
  }

  @Test
  @PactVerification("customerServiceCreateSiteProvider")
  public void runTest() throws IOException {
    Assert.assertEquals(200, new ConsumerClient(url).post(path));
  }
}
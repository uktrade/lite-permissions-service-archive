package uk.gov.bis.lite.permissions.pactx;

import static io.dropwizard.testing.FixtureHelpers.fixture;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;


public class CreateSitePactTest {

  private static String siteParam = fixture("fixture/pactx/siteParam.json");
  private static String url = "http://localhost:8080";
  private static String path = "/customer-sites/{customerId}";

  @Rule
  public PactProviderRule provider = new PactProviderRule("customer-create-site", "localhost", 8080, this);

  @Pact(provider="customer-create-site", consumer="permissions")
  public PactFragment createFragment(PactDslWithProvider builder) {
    return builder
        .given("test state")
        .uponReceiving("CreateSitePactTest test interaction")
        .path(path)
        .method("POST")
        .body(siteParam)
        .willRespondWith()
        .status(200)
        .toFragment();
  }

  @Test
  @PactVerification("customer-create-site")
  public void runTest() throws IOException {
    Assert.assertEquals(200, new ConsumerClient(url).postWithBody(path, siteParam, ContentType.APPLICATION_JSON));
  }
}
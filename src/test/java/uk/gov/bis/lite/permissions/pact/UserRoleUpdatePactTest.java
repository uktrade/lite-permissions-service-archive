package uk.gov.bis.lite.permissions.pact;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;


public class UserRoleUpdatePactTest {

  private static String url = "http://localhost:8080";
  private static String path = "/user-roles/user/{userId}/site/{siteRef}";

  @Rule
  public PactProviderRule provider = new PactProviderRule("customerServiceUserRoleUpdateProvider", "localhost", 8080, this);

  @Pact(provider="customerServiceUserRoleUpdateProvider", consumer="permissionsServiceUserRoleUpdateConsumer")
  public PactFragment createFragment(PactDslWithProvider builder) {
    return builder
        .given("test state")
        .uponReceiving("UserRoleUpdatePactTest test interaction")
        .path(path)
        .method("POST")
        .willRespondWith()
        .status(200)
        .toFragment();
  }

  @Test
  @PactVerification("customerServiceUserRoleUpdateProvider")
  public void runTest() throws IOException {
    Assert.assertEquals(200, new ConsumerClient(url).post(path));
  }
}

package uk.gov.bis.lite.permissions.pact.consumer;

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
 * CustomerUserRolePactTest
 */
public class CustomerUserRolePactTest extends CustomerBasePactTest {

  private static final String PROVIDER = "lite-customer-service";

  private static final String ADMIN_USER_ID = "ADMIN_USER_ID";
  private static final String USER_ID = "USER123";
  private static final String SITE_REF = "SITE123";

  private CustomerService customerService;

  @Rule
  public final PactProviderRule mockProvider = new PactProviderRule(PROVIDER, this);

  @Before
  public void before() {
    customerService = new CustomerServiceImpl(ClientBuilder.newClient(), mockProvider.getConfig().url());
  }

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment updateUserRoleSuccess(PactDslWithProvider builder) {

    return builder
        .given("user role update request is valid")
        .uponReceiving("request to update user role")
        .path("/user-roles/user/" + USER_ID + "/site/" + SITE_REF)
        .headers(headers())
        .method("POST")
        .body(userRoleParamPactDsl())
        .willRespondWith()
        .status(200)
        .toFragment();
  }

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment updateUserRoleFail(PactDslWithProvider builder) {

    return builder
        .given("user role update request is invalid")
        .uponReceiving("request to update user role")
        .path("/user-roles/user/" + USER_ID + "/site/" + SITE_REF)
        .headers(headers())
        .method("POST")
        .body(userRoleParamPactDsl())
        .willRespondWith()
        .status(400)
        .toFragment();
  }

  @Test
  @PactVerification(value = PROVIDER, fragment = "updateUserRoleSuccess")
  public void testUpdateUserRoleSuccessServicePact() throws Exception {
    assertThat(customerService.updateUserRole(getOgelSubmission())).isTrue();
  }

  @Test
  @PactVerification(value = PROVIDER, fragment = "updateUserRoleFail")
  public void testUpdateUserRoleFailServicePact() throws Exception {
    assertThat(customerService.updateUserRole(getOgelSubmission())).isFalse();
  }

  private OgelSubmission getOgelSubmission() {
    OgelSubmission sub = getOgelSubmission(getRegisterParam(fixture(FIXTURE_REGISTER_PARAM_EXISTING)));
    sub.setAdminUserId(ADMIN_USER_ID);
    sub.setSiteRef(SITE_REF);
    sub.setUserId(USER_ID);
    return sub;
  }

  private PactDslJsonBody userRoleParamPactDsl() {
    // Requires 'ADMIN' value because API UserRoleParam param contains enum RoleType [ADMIN, SUBMITTER, PREPARER]
    return new PactDslJsonBody()
        .stringType("adminUserId")
        .stringType("roleType", "ADMIN")
        .asBody();
  }

}

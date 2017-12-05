package uk.gov.bis.lite.permissions.pact.consumer;

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
import uk.gov.bis.lite.permissions.JwtTestHelper;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.CustomerServiceImpl;

import java.util.Optional;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;

/**
 * CustomerSitePactTest
 */
public class CustomerSitePactTest extends CustomerBasePactTest {

  private CustomerService customerService;

  private static final String CUSTOMER_ID = "CUSTOMER_ID";
  private static final String SITE_ID_VALUE = "SITE_ID_VALUE";

  private static final String PROVIDER = "lite-customer-service";
  private static final String CONSUMER = "lite-permissions-service";

  private static final String JWT_SHARED_SECRET = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";

  @Rule
  public final PactProviderRule mockProvider = new PactProviderRule(PROVIDER, this);

  @Before
  public void before() {
    customerService = new CustomerServiceImpl(ClientBuilder.newClient(), mockProvider.getConfig().url(), JWT_SHARED_SECRET);
  }

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment createSiteSuccess(PactDslWithProvider builder) {

    return builder
        .given("new site is valid")
        .uponReceiving("request to create a new site")
        .path("/customer-sites/" + CUSTOMER_ID)
        .matchHeader(HttpHeaders.AUTHORIZATION, JwtTestHelper.JWT_AUTHORIZATION_HEADER_REGEX, JwtTestHelper.JWT_AUTHORIZATION_HEADER_VALUE)
        .headers(headers())
        .method("POST")
        .query("userId=userId")
        .body(siteParamPactDsl())
        .willRespondWith()
        .headers(headers())
        .status(200)
        .body(siteViewPactDsl())
        .toFragment();
  }


  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment createSiteFail(PactDslWithProvider builder) {

    return builder
        .given("new site is invalid")
        .uponReceiving("request to create a new site")
        .path("/customer-sites/" + CUSTOMER_ID)
        .matchHeader(HttpHeaders.AUTHORIZATION, JwtTestHelper.JWT_AUTHORIZATION_HEADER_REGEX, JwtTestHelper.JWT_AUTHORIZATION_HEADER_VALUE)
        .headers(headers())
        .method("POST")
        .query("userId=")
        .body(siteParamPactDsl())
        .willRespondWith()
        .status(400)
        .toFragment();
  }

  @Test
  @PactVerification(value = PROVIDER, fragment = "createSiteSuccess")
  public void testCreateSiteSuccessServicePact() throws Exception {
    OgelSubmission sub = ogelSubmission();
    sub.setCustomerRef(CUSTOMER_ID);
    Optional<String> refOpt = customerService.createSite(sub);
    assertThat(refOpt).hasValue(SITE_ID_VALUE);
  }


  @Test
  @PactVerification(value = PROVIDER, fragment = "createSiteFail")
  public void testCreateSiteFailServicePact() throws Exception {
    OgelSubmission sub = ogelSubmission();
    sub.setCustomerRef(CUSTOMER_ID);
    sub.setUserId("");
    assertThat(customerService.createSite(sub)).isNotPresent();
  }

  private PactDslJsonBody siteParamPactDsl() {
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

  private PactDslJsonBody siteViewPactDsl() {
    return new PactDslJsonBody()
        .stringType("siteId", SITE_ID_VALUE)
        .asBody();
  }
}
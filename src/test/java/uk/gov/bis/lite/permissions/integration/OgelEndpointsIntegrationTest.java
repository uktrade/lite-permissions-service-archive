package uk.gov.bis.lite.permissions.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static uk.gov.bis.lite.permissions.spire.SpireLicenceUtil.generateToken;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Test;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.api.view.OgelSubmissionView;

import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Integration test for the following resources:
 * <pre>
 * GET     /ogel-registrations/user/{userId} (uk.gov.bis.lite.permissions.resource.OgelRegistrationResource)
 * DELETE  /ogel-submissions (uk.gov.bis.lite.permissions.resource.OgelSubmissionResource)
 * GET     /ogel-submissions (uk.gov.bis.lite.permissions.resource.OgelSubmissionResource)
 * DELETE  /ogel-submissions/{id} (uk.gov.bis.lite.permissions.resource.OgelSubmissionResource)
 * GET     /ogel-submissions/{id} (uk.gov.bis.lite.permissions.resource.OgelSubmissionResource)
 * POST    /register-ogel (uk.gov.bis.lite.permissions.resource.RegisterOgelResource)
 * </pre>
 */
public class OgelEndpointsIntegrationTest extends BaseIntegrationTest {

  private final String OGEL_REG_URL = "/ogel-registrations/user/";
  private final String REGISTER_OGEL_URL = "/register-ogel";
  private final String OGEL_SUBMISSION_URL = "/ogel-submissions/";
  private final String USER_ID = "112233";
  private final String INVALID_USER_ID = "111000";
  private final String SUB_ID = "1";
  private static String JWT_SHARED_SECRET = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";

  @Test
  public void registerOgelSuccessImmediate() throws Exception {
    initRegisterOgelStubs();
    Response response = JerseyClientBuilder.createClient()
        .target(localUrl(REGISTER_OGEL_URL))
        .queryParam("callbackUrl", "http://localhost:" + wireMockClassRule.port() + "/callback")
        .request()
        .post(Entity.entity(fixture("fixture/integration/registerOgel/registerOgelNewCustomer.json"), MediaType.APPLICATION_JSON_TYPE));

    assertThat(response.getStatus()).isEqualTo(200);

    JerseyInvocation.Builder ogelSubmissionRequest = JerseyClientBuilder
        .createClient()
        .register(HttpAuthenticationFeature.basic("user", "password"))
        .target(localUrl(OGEL_SUBMISSION_URL + SUB_ID))
        .request();

    await().with().pollInterval(1, SECONDS).atMost(30, SECONDS).until(() -> ogelSubmissionRequest
        .get()
        .readEntity(OgelSubmissionView.class)
        .getStatus()
        .equals("COMPLETE")
    );

    Response ogelSubmissionResponse = ogelSubmissionRequest.get();
    assertThat(ogelSubmissionResponse.getStatus()).isEqualTo(200);

    OgelSubmissionView actual = ogelSubmissionResponse.readEntity(OgelSubmissionView.class);
    assertThat(actual.getSpireRef()).isEqualTo("TEST2017/12345");
    assertThat(actual.getCustomerRef()).isEqualTo("SAR1");
    assertThat(actual.getSiteRef()).isEqualTo("SITE12018");
    assertThat(actual.getStatus()).isEqualTo("COMPLETE");
    assertThat(actual.getUserId()).isEqualTo("testUser");
    assertThat(actual.getOgelType()).isEqualTo("ogelType");

    verify(postRequestedFor(urlEqualTo("/create-customer"))
        .withRequestBody(equalToJson(fixture("fixture/integration/registerOgel/createNewCustomerRequest.json"))));
    verify(postRequestedFor(urlEqualTo("/customer-sites/SAR1?userId=adminUserId"))
        .withRequestBody(equalToJson(fixture("fixture/integration/registerOgel/createNewSiteRequest.json"))));
    verify(postRequestedFor(urlEqualTo("/user-roles/user/testUser/site/SITE12018"))
        .withRequestBody(equalToJson(fixture("fixture/integration/registerOgel/updateUserRoleRequest.json"))));
    verify(postRequestedFor(urlEqualTo("/spire/fox/ispire/SPIRE_CREATE_OGEL_APP"))
        .withRequestBody(equalToXml(fixture("fixture/integration/spire/createOgelRegRequest.xml"))));
    verify(postRequestedFor(urlEqualTo("/callback"))
        .withRequestBody(equalToJson(fixture("fixture/integration/registerOgel/callBackRequest.json"))));
  }

  @Test
  public void getOgelRegistrationsValidUser() {
    stubFor(post(urlEqualTo("/spire/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(containing(USER_ID))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/xml")
            .withBody(fixture("fixture/integration/spire/getOgelRegistrationReponse.xml"))));

    Response response = JerseyClientBuilder.createClient()
        .target(localUrl(OGEL_REG_URL + USER_ID))
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, USER_ID))
        .get();

    List<OgelRegistrationView> actualResponse = response.readEntity(new GenericType<List<OgelRegistrationView>>(){});
    OgelRegistrationView ogelRegistrationReponse = actualResponse.stream()
        .findFirst()
        .get();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(ogelRegistrationReponse.getOgelType()).isEqualTo("DUMMY_OGL");
    assertThat(ogelRegistrationReponse.getStatus().toString()).isEqualTo("EXTANT");
    assertThat(ogelRegistrationReponse.getCustomerId()).isEqualTo("DUMMY_SAR_REF");
    assertThat(ogelRegistrationReponse.getSiteId()).isEqualTo("DUMMY_SAR_SITE");
    assertThat(ogelRegistrationReponse.getRegistrationReference()).isEqualTo("DUMMY_REGISTRATION_REF");

    verify(postRequestedFor(urlEqualTo("/spire/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(equalToXml(fixture("fixture/integration/spire/getOgelRegistrationsValidUserRequest.xml"))));
  }

  @Test
  public void getOgelRegistrationInvalidUser() {
    stubFor(post(urlEqualTo("/spire/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(containing(INVALID_USER_ID))
        .willReturn(aResponse()
            .withStatus(400)));

    Response response = JerseyClientBuilder.createClient()
        .target(localUrl(OGEL_REG_URL + INVALID_USER_ID))
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, INVALID_USER_ID))
        .get();

    assertThat(response.getStatus()).isEqualTo(400);

    verify(postRequestedFor(urlEqualTo("/spire/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(equalToXml(fixture("fixture/integration/spire/getOgelRegistrationsInvalidUserRequest.xml"))));
  }

  @Test
  public void getOgelRegistrationNoAuthHeader() {
    Response response = JerseyClientBuilder.createClient()
        .target(localUrl(OGEL_REG_URL + USER_ID))
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void getOgelRegistrationJwtUserIdMismatch() {
    Response response = JerseyClientBuilder.createClient()
        .target(localUrl(OGEL_REG_URL + USER_ID))
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, INVALID_USER_ID))
        .get();

    assertThat(response.getStatus()).isEqualTo(401);

    Map<String, String> map = response.readEntity(new GenericType<Map<String, String>>(){});
    assertThat(map.entrySet().size()).isEqualTo(2);
    assertThat(map.get("code")).isEqualTo("401");
    assertThat(map.get("message")).contains("userId \"" + USER_ID + "\" does not match value supplied in token (" + INVALID_USER_ID + ")");

  }

  private void initRegisterOgelStubs() {
    // return customer not found for new customer
    stubFor(get(urlEqualTo("/search-customers/registered-number/GB6788"))
        .willReturn(aResponse()
            .withStatus(400)));

    // create new customer with new sarRef
    stubFor(post(urlEqualTo("/create-customer"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(fixture("fixture/integration/registerOgel/createNewCustomerResponse.json"))));

    // after successful create-customer proceed to createSite and respond with valid siteId/siteRef
    stubFor(post(urlEqualTo("/customer-sites/SAR1?userId=adminUserId"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(fixture("fixture/integration/registerOgel/createNewSiteResponse.json"))));

    // update userRole
    stubFor(post(urlEqualTo("/user-roles/user/testUser/site/SITE12018"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(fixture("fixture/integration/registerOgel/userRoleResponse.json"))));


    //return registration_ref on sucessful spire call to create OGEL
    stubFor(post(urlEqualTo("/spire/fox/ispire/SPIRE_CREATE_OGEL_APP"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/xml")
            .withBody(fixture("fixture/integration/spire/createOgelRegResponse.xml"))));

    //submission status complete. now callback
    stubFor(post(urlEqualTo("/callback"))
        .willReturn(aResponse().withStatus(200)));
  }
}

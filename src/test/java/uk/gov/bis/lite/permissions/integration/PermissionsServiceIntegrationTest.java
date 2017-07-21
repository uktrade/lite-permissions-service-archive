package uk.gov.bis.lite.permissions.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.flywaydb.core.Flyway;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.PermissionsApp;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.api.view.OgelSubmissionView;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PermissionsServiceIntegrationTest {

  private static final String OGEL_REG_URL = "http://localhost:8080/ogel-registrations/user/";
  private static final String REGISTER_OGEL_URL = "http://localhost:8080/register-ogel";
  private static final String OGEL_SUBMISSION_URL = "http://localhost:8080/ogel-submissions/";
  private static final String USER_ID = "112233";
  private static final String INVALID_USER_ID = "111000";
  private static final String SUB_ID = "1";

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(9000);

  @Rule
  public final DropwizardAppRule<PermissionsAppConfig> RULE =
      new DropwizardAppRule<>(PermissionsApp.class, resourceFilePath("service-test.yaml"));

  @Before
  public void setupDatabase() {
    DataSourceFactory f = RULE.getConfiguration().getDataSourceFactory();
    Flyway flyway = new Flyway();
    flyway.setDataSource(f.getUrl(), f.getUser(), f.getPassword());
    flyway.migrate();
  }

  @Test
  public void registerOgelSuccessImmediate() throws Exception {
    initRegisterOgelStubs();
    Response response = JerseyClientBuilder.createClient()
        .target(REGISTER_OGEL_URL)
        .queryParam("callbackUrl", "http://localhost:9000/callback")
        .request()
        .post(Entity.entity(fixture("fixture/integration/registerOgel/registerOgelNewCustomer.json"), MediaType.APPLICATION_JSON_TYPE));

    assertThat(response.getStatus()).isEqualTo(200);

    JerseyInvocation.Builder ogelSubmissionRequest = JerseyClientBuilder
        .createClient()
        .register(HttpAuthenticationFeature.basic("user", "password"))
        .target(OGEL_SUBMISSION_URL + SUB_ID)
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
        .withRequestBody(containing(fixture("fixture/integration/registerOgel/createNewCustomerRequest.json"))));
    verify(postRequestedFor(urlEqualTo("/customer-sites/SAR1?userId=adminUserId"))
        .withRequestBody(containing(fixture("fixture/integration/registerOgel/createNewSiteRequest.json"))));
    verify(postRequestedFor(urlEqualTo("/user-roles/user/testUser/site/SITE12018"))
        .withRequestBody(containing(fixture("fixture/integration/registerOgel/updateUserRoleRequest.json"))));
    verify(postRequestedFor(urlEqualTo("/spireuat/fox/ispire/SPIRE_CREATE_OGEL_APP"))
        .withRequestBody(containing(fixture("fixture/integration/spire/createOgelRegRequest.xml"))));
    verify(postRequestedFor(urlEqualTo("/callback"))
        .withRequestBody(containing(fixture("fixture/integration/registerOgel/callBackRequest.json"))));
  }

  @Test
  public void getOgelRegistrationsValidUser() {
    stubFor(post(urlEqualTo("/spireuat/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(containing(USER_ID))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/xml")
            .withBody(fixture("fixture/integration/spire/getOgelRegistrationReponse.xml"))));

    Response response = JerseyClientBuilder.createClient()
        .target(OGEL_REG_URL + USER_ID)
        .request()
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

    verify(postRequestedFor(urlEqualTo("/spireuat/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(containing(fixture("fixture/integration/spire/getOgelRegistrationsValidUserRequest.xml"))));
  }

  @Test
  public void getOgelRegistrationInvalidUser() {
    stubFor(post(urlEqualTo("/spireuat/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(containing(INVALID_USER_ID))
        .willReturn(aResponse()
            .withStatus(500)));

    Response response = JerseyClientBuilder.createClient()
        .target(OGEL_REG_URL + INVALID_USER_ID)
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(500);

    verify(postRequestedFor(urlEqualTo("/spireuat/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(containing(fixture("fixture/integration/spire/getOgelRegistrationsInvalidUserRequest.xml"))));
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
    stubFor(post(urlEqualTo("/spireuat/fox/ispire/SPIRE_CREATE_OGEL_APP"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/xml")
            .withBody(fixture("fixture/integration/spire/createOgelRegResponse.xml"))));

    //submission status complete. now callback
    stubFor(post(urlEqualTo("/callback"))
        .willReturn(aResponse().withStatus(200)));
  }
}

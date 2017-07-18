package uk.gov.bis.lite.permissions;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.flywaydb.core.Flyway;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
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
  private static final String REGISTRATION_REF = "112233";
  private static final String INVALID_REG_REF = "111000";
  private static final String SUB_ID = "1";

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(9000);

  @Rule
  public final DropwizardAppRule<PermissionsAppConfig> RULE =
      new DropwizardAppRule<>(PermissionsApp.class, resourceFilePath("service-test.yaml"));

  @BeforeClass
  public static void setUpMocks() {
    wireMockRule.stubFor(post(urlEqualTo("/spireuat/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(containing(REGISTRATION_REF))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/xml")
            .withBody(fixture("fixture/integration/spire/getOgelRegistrationReponse.xml"))));

    // return customer not found for new customer
    wireMockRule.stubFor(get(urlEqualTo("/search-customers/registered-number/GB6788"))
    .willReturn(aResponse()
    .withStatus(400)));

    // create new customer with new sarRef
    wireMockRule.stubFor(post(urlEqualTo("/create-customer"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(fixture("fixture/integration/registerOgel/createNewCustomerResponse.json"))));

    // after successful create-customer proceed to createSite and respond with valid siteId/siteRef
    wireMockRule.stubFor(post(urlEqualTo("/customer-sites/SAR1?userId=adminUserId"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(fixture("fixture/integration/registerOgel/createNewSiteResponse.json"))));

    // update userRole
    wireMockRule.stubFor(post(urlEqualTo("/user-roles/user/testUser/site/SITE12018"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(fixture("fixture/integration/registerOgel/userRoleResponse.json"))));

    //return registration_ref on sucessful spire call to create OGEL
    wireMockRule.stubFor(post(urlEqualTo("/spireuat/fox/ispire/SPIRE_CREATE_OGEL_APP"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/xml")
            .withBody(fixture("fixture/integration/spire/createOgelRegResponse.xml"))));

    //submission status complete. now callback
    wireMockRule.stubFor(post(urlEqualTo("/callback"))
        .willReturn(aResponse().withStatus(200)));
  }

  @Before
  public void setupDatabase() {
    DataSourceFactory f = RULE.getConfiguration().getDataSourceFactory();
    Flyway flyway = new Flyway();
    flyway.setDataSource(f.getUrl(), f.getUser(), f.getPassword());
    flyway.migrate();
  }

  @Test
  public void registerOgelSuccessImmediate() throws Exception{
    Response response = JerseyClientBuilder.createClient()
        .target(REGISTER_OGEL_URL)
        .queryParam("callbackUrl","http://localhost:9000/callback")
        .request()
        .post(Entity.entity(fixture("fixture/integration/registerOgel/registerOgelNewCustomer.json"), MediaType.APPLICATION_JSON_TYPE));

    Thread.sleep(5000);
    assertThat(response.getStatus()).isEqualTo(200);

    Response ogelSubmissionResponse = JerseyClientBuilder
        .createClient()
        .register(HttpAuthenticationFeature.basic("user", "password"))
        .target(OGEL_SUBMISSION_URL+SUB_ID)
        .request()
        .get();

    assertThat(ogelSubmissionResponse.getStatus()).isEqualTo(200);

    OgelSubmissionView actual = ogelSubmissionResponse.readEntity(OgelSubmissionView.class);
    assertThat(actual.getSpireRef()).isEqualTo("TEST2017/12345");
    assertThat(actual.getCustomerRef()).isEqualTo("SAR1");
    assertThat(actual.getSiteRef()).isEqualTo("SITE12018");
    assertThat(actual.getStatus()).isEqualTo("COMPLETE");
    assertThat(actual.getUserId()).isEqualTo("testUser");
    assertThat(actual.getOgelType()).isEqualTo("ogelType");
  }

  @Test
  public void getOgelRegistrationsValidUser() {
    Response response = JerseyClientBuilder.createClient()
        .target(OGEL_REG_URL+REGISTRATION_REF)
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
  }

  @Test
  public void getOgelRegistrationInvalidUser() {
    Response response = JerseyClientBuilder.createClient()
        .target(OGEL_REG_URL+INVALID_REG_REF)
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(500);
  }
}

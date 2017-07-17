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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PermissionsServiceIntegrationTest {

  private static final String SPIRE_FIXTURES = "fixture/integration/spire/";
  private static final String OGEL_REG_URL = "http://localhost:8080/ogel-registrations/user/";
  private static final String REGISTER_OGEL = "http://localhost:8080/register-ogel";

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(9000);

  @Rule
  public final DropwizardAppRule<PermissionsAppConfig> RULE =
      new DropwizardAppRule<>(PermissionsApp.class, resourceFilePath("service-test.yaml"));

  @BeforeClass
  public static void setup() {
    wireMockRule.stubFor(post(urlEqualTo("/spireuat/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(containing("112233"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/xml")
            .withBody(fixture(SPIRE_FIXTURES + "getOgelRegistration.xml"))));

    wireMockRule.stubFor(get(urlEqualTo("/search-customers/registered-number/GB6788"))
    .willReturn(aResponse()
    .withStatus(400)));

    wireMockRule.stubFor(post(urlEqualTo("/create-customer"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(fixture("fixture/integration/register/createNewCustomerResponse.json"))));

    wireMockRule.stubFor(post(urlEqualTo("/customer-sites/SAR1?userId=adminUserId"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(fixture("fixture/integration/register/createNewSiteResponse.json"))));

    wireMockRule.stubFor(post(urlEqualTo("/user-roles/user/test66/site/SITE12018"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(fixture("fixture/integration/register/userRoleResponse.json"))));

    wireMockRule.stubFor(post(urlEqualTo("/spireuat/fox/ispire/SPIRE_CREATE_OGEL_APP"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/xml")
            .withBody(fixture("fixture/integration/spire/createOgelRegistration.xml"))));
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
        .target(REGISTER_OGEL)
        .queryParam("callbackUrl","http://localhost:8080/registration/callback?transaction=abc&secret=xyz")
        .request()
        .post(Entity.entity(fixture("fixture/integration/register/registerOgelNewCustomer.json"), MediaType.APPLICATION_JSON_TYPE));

//    Thread.sleep(5000);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  public void getOgelRegistrationsValidUser() {
    Response response = new JerseyClientBuilder()
        .build()
        .target(OGEL_REG_URL+112233)
        .request()
        .get();

    List<OgelRegistrationView> actualResponse = response.readEntity(new GenericType<List<OgelRegistrationView>>(){});
    OgelRegistrationView ogelResponse = actualResponse.stream()
        .findFirst()
        .get();

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(ogelResponse.getOgelType().equals("DUMMY_OGL"));
    assertThat(ogelResponse.getStatus().equals("DUMMY_STATUS"));
    assertThat(ogelResponse.getCustomerId().equals("DUMMY_SAR_REF"));
    assertThat(ogelResponse.getRegistrationReference().equals("DUMMY_REGISTRATION_REF"));
  }

  @Test
  public void getOgelRegistrationInvalidUser() {
    Response response = new JerseyClientBuilder()
        .build()
        .target(OGEL_REG_URL+111)
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(500);
  }
}

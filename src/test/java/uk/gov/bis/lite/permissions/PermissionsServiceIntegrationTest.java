package uk.gov.bis.lite.permissions;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;

import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

public class PermissionsServiceIntegrationTest {

  private static final String PATH = "fixture/soap/";
  private static final String OGEL_REG_URL = "http://localhost:8080/ogel-registrations/user/111222333";

  @ClassRule
  public static final WireMockRule wireMockRule = new WireMockRule(9000);

  @Rule
  public final DropwizardAppRule<PermissionsAppConfig> RULE =
      new DropwizardAppRule<>(PermissionsApp.class, resourceFilePath("service-test.yaml"));

  @BeforeClass
  public static void setup() {
    wireMockRule.stubFor(post(urlEqualTo("/spireuat/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(containing("111222333"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/xml")
            .withBody(fixture(PATH + "getOgelRegistration.xml"))));
  }

  @Test
  public void getOgelRegistrationsValidUser() {

    Response response = new JerseyClientBuilder()
        .build()
        .target(OGEL_REG_URL)
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
        .target("http://localhost:8080/ogel-registrations/user/111")
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(500);
  }
}

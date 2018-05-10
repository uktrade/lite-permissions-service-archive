package uk.gov.bis.lite.permissions.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Test;
import uk.gov.bis.lite.common.jwt.LiteJwtConfig;
import uk.gov.bis.lite.common.jwt.LiteJwtUserHelper;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public class OgelRegistrationIntegrationTest extends BaseIntegrationTest {

  private static final String USER_ID = "112233";
  private static final String INVALID_USER_ID = "111000";
  private static final String OGEL_REGISTRATIONS_URL = "/ogel-registrations/user/";
  private static final String JWT_SHARED_SECRET = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";

  private final LiteJwtUserHelper liteJwtUserHelper = new LiteJwtUserHelper(new LiteJwtConfig(JWT_SHARED_SECRET, "some-lite-service"));

  private String jwtAuthorizationHeader(String userId) {
    return liteJwtUserHelper.generateTokenInAuthHeaderFormat(Util.getTestLiteJwtUser(userId));
  }

  @Test
  public void getOgelRegistrationsValidUser() {
    stubFor(post(urlEqualTo("/spire/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(containing(USER_ID))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/xml")
            .withBody(fixture("fixture/soap/SPIRE_OGEL_REGISTRATIONS/getOgelRegistrationReponse.xml"))));

    Response response = JerseyClientBuilder.createClient()
        .target(localUrl(OGEL_REGISTRATIONS_URL + USER_ID))
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader(USER_ID))
        .get();

    List<OgelRegistrationView> actualResponse = response.readEntity(new GenericType<List<OgelRegistrationView>>() {
    });
    assertThat(actualResponse).hasSize(1);

    OgelRegistrationView ogelRegistrationResponse = actualResponse.get(0);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(ogelRegistrationResponse.getOgelType()).isEqualTo("DUMMY_OGL");
    assertThat(ogelRegistrationResponse.getStatus().toString()).isEqualTo("EXTANT");
    assertThat(ogelRegistrationResponse.getCustomerId()).isEqualTo("DUMMY_SAR_REF");
    assertThat(ogelRegistrationResponse.getSiteId()).isEqualTo("DUMMY_SAR_SITE");
    assertThat(ogelRegistrationResponse.getRegistrationReference()).isEqualTo("DUMMY_REGISTRATION_REF");

    verify(postRequestedFor(urlEqualTo("/spire/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(equalToXml(fixture("fixture/soap/SPIRE_OGEL_REGISTRATIONS/getOgelRegistrationsValidUserRequest.xml"))));
  }

  @Test
  public void getOgelRegistrationInvalidUser() {
    stubFor(post(urlEqualTo("/spire/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(containing(INVALID_USER_ID))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/xml")
            .withBody(fixture("fixture/soap/SPIRE_OGEL_REGISTRATIONS/userIdDoesNotExist.xml"))));

    Response response = JerseyClientBuilder.createClient()
        .target(localUrl(OGEL_REGISTRATIONS_URL + INVALID_USER_ID))
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader(INVALID_USER_ID))
        .get();

    assertThat(response.getStatus()).isEqualTo(404);

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "404");
    assertThat(map).containsEntry("message", "Unable to find user with user id " + INVALID_USER_ID);

    verify(postRequestedFor(urlEqualTo("/spire/fox/ispire/SPIRE_OGEL_REGISTRATIONS"))
        .withRequestBody(equalToXml(fixture("fixture/soap/SPIRE_OGEL_REGISTRATIONS/getOgelRegistrationsInvalidUserRequest.xml"))));
  }

  @Test
  public void getOgelRegistrationNoAuthHeader() {
    Response response = JerseyClientBuilder.createClient()
        .target(localUrl(OGEL_REGISTRATIONS_URL + USER_ID))
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.readEntity(String.class)).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void getOgelRegistrationJwtUserIdMismatch() {
    Response response = JerseyClientBuilder.createClient()
        .target(localUrl(OGEL_REGISTRATIONS_URL + USER_ID))
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader(INVALID_USER_ID))
        .get();

    assertThat(response.getStatus()).isEqualTo(401);

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "401");
    assertThat(map).containsEntry("message", "userId " + USER_ID + " does not match value supplied in token " + INVALID_USER_ID);
  }

}

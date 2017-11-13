package uk.gov.bis.lite.permissions.resource;


import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.bis.lite.permissions.spire.SpireLicenceUtil.generateToken;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.common.jwt.LiteJwtAuthFilterHelper;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.mocks.RegistrationsServiceMock;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

public class ResourceOgelRegistrationTest {

  private int OK = Response.Status.OK.getStatusCode();
  private int NOT_FOUND = Response.Status.NOT_FOUND.getStatusCode();
  private int UNAUTHORIZED = Response.Status.UNAUTHORIZED.getStatusCode();

  private static int MOCK_REGISTRATIONS_NUMBER = 3;
  private static String MOCK_REGISTRATION_TAG = "SPIRE";
  private static String JWT_SHARED_SECRET = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";
  private static RegistrationsServiceMock mockRegistrationsService = new RegistrationsServiceMock(MOCK_REGISTRATION_TAG, MOCK_REGISTRATIONS_NUMBER);

  @Before
  public void setUp() throws Exception {
    mockRegistrationsService.resetState();
  }

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
      .addProvider(new AuthDynamicFeature(LiteJwtAuthFilterHelper.buildAuthFilter(JWT_SHARED_SECRET)))
      .addProvider(new AuthValueFactoryProvider.Binder<>(LiteJwtUser.class))
      .addResource(new OgelRegistrationResource(mockRegistrationsService)).build();

  @Test
  public void viewOgelRegistrations() {
    String token = generateToken(JWT_SHARED_SECRET, "1");
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .request()
        .header("Authorization", "Bearer " + token)
        .get();
    assertThat(status(response)).isEqualTo(OK);
    assertThat(getOgelRegistrationsResponse(response).size()).isEqualTo(MOCK_REGISTRATIONS_NUMBER);
  }

  @Test
  public void viewOgelRegistrationsNoAuthHeader() {
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .request()
        .get();
    assertThat(status(response)).isEqualTo(UNAUTHORIZED);
  }

  @Test
  public void viewOgelRegistrationsJwtUserIdMismatch() {
    String token = generateToken(JWT_SHARED_SECRET, "999");
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .request()
        .header("Authorization", "Bearer " + token)
        .get();
    assertThat(status(response)).isEqualTo(UNAUTHORIZED);
    Map<String, String> map = response.readEntity(new GenericType<Map<String, String>>(){});
    assertThat(map.entrySet().size()).isEqualTo(2);
    assertThat(map.get("code")).isEqualTo(Integer.toString(UNAUTHORIZED));
    assertThat(map.get("message")).contains("userId \"1\" does not match value supplied in token (999)");
  }

  @Test
  public void viewOgelRegistrationsWithParam1() {
    String token = generateToken(JWT_SHARED_SECRET, "1");
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .queryParam("registrationReference", MOCK_REGISTRATION_TAG + "1")
        .request()
        .header("Authorization", "Bearer " + token)
        .get();
    assertThat(status(response)).isEqualTo(OK);
    assertThat(getOgelRegistrationsResponse(response).size()).isEqualTo(1);
  }

  @Test
  public void viewOgelRegistrationsWithParam2() {
    mockRegistrationsService.setUserNotFound(true); // Update mockRegistrationsService first
    String token = generateToken(JWT_SHARED_SECRET, "1");
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .queryParam("registrationReference", "NOT_THERE")
        .request()
        .header("Authorization", "Bearer " + token)
        .get();
    assertThat(status(response)).isEqualTo(NOT_FOUND);
  }

  @Test
  public void viewOgelRegistrationsWithParam3() {
    mockRegistrationsService.setNoResults(true);
    String token = generateToken(JWT_SHARED_SECRET, "1");
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .queryParam("registrationReference", MOCK_REGISTRATION_TAG + "1")
        .request()
        .header("Authorization", "Bearer " + token)
        .get();
    assertThat(status(response)).isEqualTo(NOT_FOUND);
  }

  @Test
  public void viewOgelRegistrationsNoResults() {
    mockRegistrationsService.setNoResults(true); // Update mockRegistrationsService first
    String token = generateToken(JWT_SHARED_SECRET, "1");
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .request()
        .header("Authorization", "Bearer " + token)
        .get();
    assertThat(status(response)).isEqualTo(OK);
    assertThat(getOgelRegistrationsResponse(response).isEmpty()).isTrue();
  }

  /**
   * Private Methods
   */
  private List<OgelRegistrationView> getOgelRegistrationsResponse(Response response) {
    return (List<OgelRegistrationView>) response.readEntity(List.class);
  }

  private int status(Response response) {
    return response.getStatus();
  }
}
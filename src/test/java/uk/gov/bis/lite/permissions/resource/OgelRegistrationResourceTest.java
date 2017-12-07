package uk.gov.bis.lite.permissions.resource;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.common.jwt.LiteJwtAuthFilterHelper;
import uk.gov.bis.lite.common.jwt.LiteJwtConfig;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.common.jwt.LiteJwtUserHelper;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.mocks.RegistrationServiceMock;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public class OgelRegistrationResourceTest {

  private static final String JWT_SHARED_SECRET = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";
  private static final RegistrationServiceMock MOCK_REGISTRATIONS_SERVICE = new RegistrationServiceMock();

  private final LiteJwtUserHelper liteJwtUserHelper = new LiteJwtUserHelper(new LiteJwtConfig(JWT_SHARED_SECRET, "some-lite-service"));

  @Before
  public void setUp() throws Exception {
    MOCK_REGISTRATIONS_SERVICE.resetState();
  }

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
      .addProvider(new AuthDynamicFeature(LiteJwtAuthFilterHelper.buildAuthFilter(JWT_SHARED_SECRET)))
      .addProvider(new AuthValueFactoryProvider.Binder<>(LiteJwtUser.class))
      .addResource(new OgelRegistrationResource(MOCK_REGISTRATIONS_SERVICE)).build();

  private String jwtAuthorizationHeader(String userId) {
    LiteJwtUser liteJwtUser = new LiteJwtUser().setUserId(userId).setEmail("example@example.com").setFullName("Mr Test");
    return liteJwtUserHelper.generateTokenInAuthHeaderFormat(liteJwtUser);
  }

  @Test
  public void viewOgelRegistrations() {
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("1"))
        .get();
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(getOgelRegistrationsResponse(response)).hasSize(1);
  }

  @Test
  public void viewOgelRegistrationsNoAuthHeader() {
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .request()
        .get();
    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.readEntity(String.class)).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void viewOgelRegistrationsJwtUserIdMismatch() {
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("999"))
        .get();
    assertThat(response.getStatus()).isEqualTo(401);

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "401");
    assertThat(map).containsEntry("message", "userId 1 does not match value supplied in token 999");
  }

  @Test
  public void viewOgelRegistrationsNoResults() {
    MOCK_REGISTRATIONS_SERVICE.setNoResults(true);
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("1"))
        .get();
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(getOgelRegistrationsResponse(response)).isEmpty();
  }

  @Test
  public void viewOgelRegistrationsWithReference() {
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .queryParam("registrationReference", "REG_REF")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("1"))
        .get();
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(getOgelRegistrationsResponse(response)).hasSize(1);
  }

  @Test
  public void viewOgelRegistrationsWithReferenceUserNotFound() {
    MOCK_REGISTRATIONS_SERVICE.setUserNotFound(true);
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .queryParam("registrationReference", "NOT_THERE")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("1"))
        .get();
    assertThat(response.getStatus()).isEqualTo(404);

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "404");
    assertThat(map).containsEntry("message", "Unable to find user with user id 1");
  }

  @Test
  public void viewOgelRegistrationsWithReferenceNoResults() {
    MOCK_REGISTRATIONS_SERVICE.setNoResults(true);
    Response response = resources.getJerseyTest()
        .target("/ogel-registrations/user/1")
        .queryParam("registrationReference", "REG_REF")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("1"))
        .get();
    assertThat(response.getStatus()).isEqualTo(404);

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "404");
    assertThat(map).containsEntry("message", "No licence with reference REG_REF found for userId 1");
  }

  /**
   * Private Methods
   */
  private List<OgelRegistrationView> getOgelRegistrationsResponse(Response response) {
    return response.readEntity(new GenericType<List<OgelRegistrationView>>() {
    });
  }

}
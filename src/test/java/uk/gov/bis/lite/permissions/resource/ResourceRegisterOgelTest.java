package uk.gov.bis.lite.permissions.resource;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.common.jwt.LiteJwtAuthFilterHelper;
import uk.gov.bis.lite.common.jwt.LiteJwtConfig;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.common.jwt.LiteJwtUserHelper;
import uk.gov.bis.lite.permissions.api.RegisterOgelResponse;
import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.mocks.RegisterServiceMock;
import uk.gov.bis.lite.permissions.mocks.SubmissionServiceMock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ResourceRegisterOgelTest {

  private static final String MOCK_USER_ID = "ID1";
  private static final String MOCK_OGEL_TYPE = "TYPE1";
  private static final String MOCK_SAR_REF = "SAR1";
  private static final String MOCK_SITE_REF = "SITE1";
  private static final RegisterServiceMock mockRegisterService = new RegisterServiceMock();
  private static final SubmissionServiceMock mockSubmissionService = new SubmissionServiceMock();
  private static final String JWT_SHARED_SECRET = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new RegisterOgelResource(mockRegisterService, mockSubmissionService))
      .addProvider(new AuthDynamicFeature(LiteJwtAuthFilterHelper.buildAuthFilter(JWT_SHARED_SECRET)))
      .addProvider(new AuthValueFactoryProvider.Binder<>(LiteJwtUser.class))
      .build();

  @Test
  public void invalidRegisterOgel() {
    Response response = request("/register-ogel", MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader())
        .post(Entity.entity(new RegisterParam(), MediaType.APPLICATION_JSON));
    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void alreadyExistsRegisterOgel() {
    mockSubmissionService.setSubmissionCurrentlyExists(true); // Update mockSubmissionService first
    Response response = request("/register-ogel", MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader())
        .post(Entity.entity(getValidRegisterOgel(), MediaType.APPLICATION_JSON));
    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void validRegisterOgel() {
    mockSubmissionService.setSubmissionCurrentlyExists(false);  // Update mockSubmissionService first
    Response response = request("/register-ogel", MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader())
        .post(Entity.entity(getValidRegisterOgel(), MediaType.APPLICATION_JSON));
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(getResponseRequestId(response)).isEqualTo("SUB1");
  }

  /**
   * private methods
   */
  private Invocation.Builder request(String url, String mediaType) {
    return target(url).request(mediaType);
  }

  private WebTarget target(String url) {
    return resources.client().target(url);
  }

  private RegisterParam getValidRegisterOgel() {
    RegisterParam reg = new RegisterParam();
    reg.setUserId(MOCK_USER_ID);
    reg.setOgelType(MOCK_OGEL_TYPE);
    reg.setExistingCustomer(MOCK_SAR_REF);
    reg.setExistingSite(MOCK_SITE_REF);
    return reg;
  }

  private String getResponseRequestId(Response response) {
    return response.readEntity(RegisterOgelResponse.class).getRequestId();
  }

  private String jwtAuthorizationHeader() {
    LiteJwtUserHelper liteJwtUserHelper = new LiteJwtUserHelper(new LiteJwtConfig(JWT_SHARED_SECRET, "some-lite-service"));
    LiteJwtUser liteJwtUser = new LiteJwtUser().setUserId("123456").setEmail("test@test.com").setFullName("Mr Test");
    return liteJwtUserHelper.generateTokenInAuthHeaderFormat(liteJwtUser);
  }
}

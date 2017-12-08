package uk.gov.bis.lite.permissions.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertLicenceViewA;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertLicenceViewB;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.generateLicenceViewA;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.generateLicenceViewB;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.common.jwt.LiteJwtAuthFilterHelper;
import uk.gov.bis.lite.common.jwt.LiteJwtConfig;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.common.jwt.LiteJwtUserHelper;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.service.LicenceService;
import uk.gov.bis.lite.permissions.service.model.LicenceResult;
import uk.gov.bis.lite.permissions.service.model.LicenceTypeParam;
import uk.gov.bis.lite.permissions.service.model.Status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public class LicenceResourceTest {

  private static final String URL = "/licences/user";
  private static final String JWT_SHARED_SECRET = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";

  private final LicenceService licenceService = mock(LicenceService.class);
  private final LiteJwtUserHelper liteJwtUserHelper = new LiteJwtUserHelper(new LiteJwtConfig(JWT_SHARED_SECRET, "some-lite-service"));

  private String jwtAuthorizationHeader(String userId) {
    LiteJwtUser liteJwtUser = new LiteJwtUser().setUserId(userId).setEmail("example@example.com").setFullName("Mr Test");
    return liteJwtUserHelper.generateTokenInAuthHeaderFormat(liteJwtUser);
  }

  @Rule
  public final ResourceTestRule rule = ResourceTestRule.builder()
      .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
      .addProvider(new AuthDynamicFeature(LiteJwtAuthFilterHelper.buildAuthFilter(JWT_SHARED_SECRET)))
      .addProvider(new AuthValueFactoryProvider.Binder<>(LiteJwtUser.class))
      .addResource(new LicenceResource(licenceService))
      .build();

  @Test
  public void noParamsSingleLicenceTest() throws Exception {
    when(licenceService.getAllLicences("123456"))
        .thenReturn(new LicenceResult(Status.OK, null, ImmutableList.of(generateLicenceViewA())));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    List<LicenceView> results = Arrays.asList(response.readEntity(LicenceView[].class));
    assertThat(results).hasSize(1);

    LicenceView lv = results.get(0);
    assertLicenceViewA(lv);
  }

  @Test
  public void noParamsMultipleLicencesTest() throws Exception {
    when(licenceService.getAllLicences("123456"))
        .thenReturn(new LicenceResult(Status.OK, null, ImmutableList.of(generateLicenceViewA(), generateLicenceViewB())));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    List<LicenceView> results = Arrays.asList(response.readEntity(LicenceView[].class));
    assertThat(results).hasSize(2);

    assertLicenceViewA(results.get(0));
    assertLicenceViewB(results.get(1));
  }

  @Test
  public void noParamsNoLicencesTest() throws Exception {
    when(licenceService.getAllLicences("123456"))
        .thenReturn(new LicenceResult(Status.OK, null, Collections.emptyList()));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    List<LicenceView> results = Arrays.asList(response.readEntity(LicenceView[].class));
    assertThat(results).hasSize(0);
  }

  @Test
  public void noParamsNotAuthenticatedTest() throws Exception {
    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("999999"))
        .get();

    assertThat(response.getStatus()).isEqualTo(401);

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "401");
    assertThat(map).containsEntry("message", "userId 123456 does not match value supplied in token 999999");
  }

  @Test
  public void refParamSingleLicenceTest() throws Exception {
    when(licenceService.getLicenceByRef("123456", "REF-123"))
        .thenReturn(new LicenceResult(Status.OK, null, ImmutableList.of(generateLicenceViewA())));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("licenceReference", "REF-123")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    List<LicenceView> results = Arrays.asList(response.readEntity(LicenceView[].class));
    assertThat(results).hasSize(1);

    assertLicenceViewA(results.get(0));
  }

  @Test
  public void refParamNoLicenceTest() throws Exception {
    when(licenceService.getLicenceByRef("123456", "REF-123"))
        .thenReturn(new LicenceResult(Status.REGISTRATION_NOT_FOUND,
            "No licence with reference REF-123 found for userId 123456",
            null));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("licenceReference", "REF-123")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("123456"))
        .get();

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "404");
    assertThat(map).containsEntry("message", "No licence with reference REF-123 found for userId 123456");
  }

  @Test
  public void refParamNoUserIdFoundTest() throws Exception {
    when(licenceService.getLicenceByRef("123456", "REF-123"))
        .thenReturn(new LicenceResult(Status.USER_ID_NOT_FOUND, "Unable to find user with user id 123456", null));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("licenceReference", "REF-123")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(404);

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "404");
    assertThat(map).containsEntry("message", "Unable to find user with user id 123456");
  }

  @Test
  public void refParamNotAuthenticatedTest() throws Exception {
    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("licenceReference", "REF-123")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("999999"))
        .get();

    assertThat(response.getStatus()).isEqualTo(401);

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "401");
    assertThat(map).containsEntry("message", "userId 123456 does not match value supplied in token 999999");
  }

  @Test
  public void typeParamSingleLicenceTest() throws Exception {
    when(licenceService.getLicencesByType("123456", LicenceTypeParam.SIEL))
        .thenReturn(new LicenceResult(Status.OK, null, ImmutableList.of(generateLicenceViewA())));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("type", "SIEL")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    List<LicenceView> results = Arrays.asList(response.readEntity(LicenceView[].class));
    assertThat(results).hasSize(1);
    assertLicenceViewA(results.get(0));
  }

  @Test
  public void typeParamNoUserFoundTest() throws Exception {
    when(licenceService.getLicencesByType("123456", LicenceTypeParam.SIEL))
        .thenReturn(new LicenceResult(Status.USER_ID_NOT_FOUND, "Unable to find user with user id 123456", null));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("type", "SIEL")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(404);

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "404");
    assertThat(map).containsEntry("message", "Unable to find user with user id 123456");
  }

  @Test
  public void typeParamInvalidTest() throws Exception {
    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("type", "OIEL")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(400);

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "400");
    assertThat(map).containsEntry("message", "query param type must be one of [SIEL]");
  }

  @Test
  public void typeParamNotAuthenticatedTest() throws Exception {
    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("type", "SIEL")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("999999"))
        .get();

    assertThat(response.getStatus()).isEqualTo(401);

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "401");
    assertThat(map).containsEntry("message", "userId 123456 does not match value supplied in token 999999");
  }

  @Test
  public void typeAndRefParamPriorityTest() throws Exception {
    when(licenceService.getLicenceByRef("123456", "REF-123"))
        .thenReturn(new LicenceResult(Status.TOO_MANY_REGISTRATIONS,
            "Too many results from spire client, expected 1 but got 2",
            null));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("type", "SIEL")
        .queryParam("licenceReference", "REF-123")
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(400);

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "400");
    assertThat(map).containsEntry("message", "Too many results from spire client, expected 1 but got 2");
  }
}

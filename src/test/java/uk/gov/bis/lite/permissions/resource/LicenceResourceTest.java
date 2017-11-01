package uk.gov.bis.lite.permissions.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertLicenceViewA;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertLicenceViewB;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.generateLicenceViewA;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.generateLicenceViewB;
import static uk.gov.bis.lite.permissions.spire.SpireLicenceUtil.generateToken;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.assertj.core.api.AssertionsForClassTypes;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.common.jwt.LiteJwtAuthFilterHelper;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.service.LicenceService;
import uk.gov.bis.lite.permissions.service.model.LicenceResult;
import uk.gov.bis.lite.permissions.service.model.LicencesResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

public class LicenceResourceTest {
  private static final String URL = "/licences";
  private static String JWT_SHARED_SECRET = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";

  private final LicenceService licenceService = mock(LicenceService.class);

  @Rule
  public final ResourceTestRule rule = ResourceTestRule.builder()
      .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
      .addProvider(new AuthDynamicFeature(LiteJwtAuthFilterHelper.buildAuthFilter(JWT_SHARED_SECRET)))
      .addProvider(new AuthValueFactoryProvider.Binder<>(LiteJwtUser.class))
      .addResource(new LicenceResource(licenceService))
      .build();

  @Test
  public void noParamsSingleLicenceTest() throws Exception {
    when(licenceService.getLicences("123456"))
        .thenReturn(LicencesResult.ok(ImmutableList.of(generateLicenceViewA())));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, "123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    List<LicenceView> results = Arrays.asList(response.readEntity(LicenceView[].class));
    assertThat(results).isNotNull();
    assertThat(results).hasSize(1);

    LicenceView lv = results.get(0);
    assertLicenceViewA(lv);
  }

  @Test
  public void noParamsMultipleLicencesTest() throws Exception {
    when(licenceService.getLicences("123456"))
        .thenReturn(LicencesResult.ok((ImmutableList.of(generateLicenceViewA(), generateLicenceViewB()))));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, "123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    List<LicenceView> results = Arrays.asList(response.readEntity(LicenceView[].class));
    assertThat(results).isNotNull();
    assertThat(results).hasSize(2);

    assertLicenceViewA(results.get(0));
    assertLicenceViewB(results.get(1));
  }

  @Test
  public void noParamsNoLicencesTest() throws Exception {
    when(licenceService.getLicences("123456"))
        .thenReturn(LicencesResult.ok((Collections.emptyList())));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, "123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    List<LicenceView> results = Arrays.asList(response.readEntity(LicenceView[].class));
    assertThat(results).isNotNull();
    assertThat(results).hasSize(0);
  }

  @Test
  public void noParamsNotAuthedTest() throws Exception {
    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, "999999"))
        .get();

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void refParamSingleLicenceTest() throws Exception {
    when(licenceService.getLicence("123456", "REF-123"))
        .thenReturn(LicenceResult.ok(generateLicenceViewA()));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("ref", "REF-123")
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, "123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    List<LicenceView> results = Arrays.asList(response.readEntity(LicenceView[].class));
    assertThat(results).isNotNull();
    assertThat(results).hasSize(1);

    assertLicenceViewA(results.get(0));
  }

  @Test
  public void refParamNoLicenceTest() throws Exception {
    when(licenceService.getLicence("123456", "REF-123"))
        .thenReturn(LicenceResult.empty());

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("ref", "REF-123")
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, "123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    List<LicenceView> results = Arrays.asList(response.readEntity(LicenceView[].class));
    assertThat(results).isNotNull();
    assertThat(results).hasSize(0);
  }

  @Test
  public void refParamNoUserIdFoundTest() throws Exception {
    when(licenceService.getLicence("123456", "REF-123"))
        .thenReturn(LicenceResult.userIdNotFound());

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("ref", "REF-123")
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, "123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void refParamNotAuthedTest() throws Exception {
    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("ref", "REF-123")
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, "999999"))
        .get();

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void typeParamSingleLicenceTest() throws Exception {
    when(licenceService.getLicences("123456", LicenceService.LicenceTypeParam.SIEL))
        .thenReturn(LicencesResult.ok(ImmutableList.of(generateLicenceViewA())));

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("type", "SIEL")
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, "123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    List<LicenceView> results = Arrays.asList(response.readEntity(LicenceView[].class));
    assertThat(results).isNotNull();
    assertThat(results).hasSize(1);
    assertLicenceViewA(results.get(0));
  }

  @Test
  public void typeParamNoUserFoundTest() throws Exception {
    when(licenceService.getLicences("123456", LicenceService.LicenceTypeParam.SIEL))
        .thenReturn(LicencesResult.userIdNotFound());

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("type", "SIEL")
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, "123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void typeParamInvalidTest() throws Exception {
    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("type", "OIEL")
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, "123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(400);

    Map<String, String> map = response.readEntity(new GenericType<Map<String, String>>(){});
    AssertionsForClassTypes.assertThat(map.entrySet().size()).isEqualTo(2);
    AssertionsForClassTypes.assertThat(map.get("code")).isEqualTo("400");
    AssertionsForClassTypes.assertThat(map.get("message")).contains("Invalid licence type");
  }

  @Test
  public void typeParamNotAuthedTest() throws Exception {
    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("type", "SIEL")
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, "999999"))
        .get();

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void typeAndRefParamPriorityTest() throws Exception {
   when(licenceService.getLicence("123456", "REF-123"))
        .thenReturn(LicenceResult.empty());

    Response response = rule.getJerseyTest()
        .target(URL + "/123456")
        .queryParam("type", "SIEL")
        .queryParam("ref", "REF-123")
        .request()
        .header("Authorization", "Bearer " + generateToken(JWT_SHARED_SECRET, "123456"))
        .get();

    assertThat(response.getStatus()).isEqualTo(200);

    List<LicenceView> results = Arrays.asList(response.readEntity(LicenceView[].class));
    assertThat(results).isNotNull();
    assertThat(results).hasSize(0);

    verify(licenceService).getLicence(eq("123456"), eq("REF-123"));
  }
}
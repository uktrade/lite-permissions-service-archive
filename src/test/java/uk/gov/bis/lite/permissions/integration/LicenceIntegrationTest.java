package uk.gov.bis.lite.permissions.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertLicenceViewA;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertLicenceViewB;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertLicenceViewC;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;
import uk.gov.bis.lite.common.jwt.LiteJwtConfig;
import uk.gov.bis.lite.common.jwt.LiteJwtUserHelper;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.api.view.LicenceView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * Integration test for the following resources:
 * <pre>
 * GET     /licences/{userId} (uk.gov.bis.lite.permissions.resource.LicenceResource)
 * </pre>
 */
public class LicenceIntegrationTest extends BaseIntegrationTest {

  private static final String LICENCES_URL = "/licences/user/";
  private static final String JWT_SHARED_SECRET = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";

  private final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
  private final LiteJwtUserHelper liteJwtUserHelper = new LiteJwtUserHelper(new LiteJwtConfig(JWT_SHARED_SECRET, "some-lite-service"));

  private String jwtAuthorizationHeader(String userId) {
    return liteJwtUserHelper.generateTokenInAuthHeaderFormat(Util.getTestLiteJwtUser(userId));
  }

  private void stubForBody(String body) {
    stubFor(post(urlEqualTo("/spire/fox/ispire/SPIRE_LICENCES"))
        .withBasicAuth("username", "password")
        .withRequestBody(matchingXPath("//SOAP-ENV:Envelope/SOAP-ENV:Body/spir:getLicences/userId")
            .withXPathNamespace("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/")
            .withXPathNamespace("spir", "http://www.fivium.co.uk/fox/webservices/ispire/SPIRE_LICENCES")
        )
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/xml; charset=utf-8")
            .withBody(body)));
  }

  private Response get(String url) {
    return RULE.client()
        .target(localUrl(url))
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("123456"))
        .get();
  }

  private Response get(String url, String queryParamName, String queryParamValue) {
    return RULE.client()
        .target(localUrl(url))
        .queryParam(queryParamName, queryParamValue)
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("123456"))
        .get();
  }

  @Test
  public void singleLicenceTest() throws Exception {
    stubForBody(fixture("fixture/soap/SPIRE_LICENCES/singleLicence.xml"));

    Response response = get(LICENCES_URL + "123456");

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getHeaderString("Content-Type")).isEqualTo("application/json");

    List<LicenceView> licences = Arrays.asList(MAPPER.readValue(response.readEntity(String.class), LicenceView[].class));
    assertThat(licences).hasSize(1);
    assertLicenceViewA(licences.get(0));
  }

  @Test
  public void multipleLicencesTest() throws Exception {
    stubForBody(fixture("fixture/soap/SPIRE_LICENCES/multipleLicences.xml"));

    Response response = get(LICENCES_URL + "123456");

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getHeaderString("Content-Type")).isEqualTo("application/json");

    List<LicenceView> licences = Arrays.asList(MAPPER.readValue(response.readEntity(String.class), LicenceView[].class));
    assertThat(licences).hasSize(3);
    assertLicenceViewA(licences.get(0));
    assertLicenceViewB(licences.get(1));
    assertLicenceViewC(licences.get(2));
  }

  @Test
  public void noLicencesTest() throws Exception {
    stubForBody(fixture("fixture/soap/SPIRE_LICENCES/noLicences.xml"));

    Response response = get(LICENCES_URL + "123456");

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getHeaderString("Content-Type")).isEqualTo("application/json");

    List<LicenceView> licences = Arrays.asList(MAPPER.readValue(response.readEntity(String.class), LicenceView[].class));
    assertThat(licences).isEmpty();
  }

  @Test
  public void noJwtTokenTest() throws Exception {
    stubForBody(fixture("fixture/soap/SPIRE_LICENCES/singleLicence.xml"));

    Response response = RULE.client()
        .target(localUrl(LICENCES_URL + "123456"))
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void mismatchedJwtSubjectToUserIdTest() throws Exception {
    stubForBody(fixture("fixture/soap/SPIRE_LICENCES/singleLicence.xml"));

    Response response = RULE.client()
        .target(localUrl(LICENCES_URL + "123456"))
        .request()
        .header(HttpHeaders.AUTHORIZATION, jwtAuthorizationHeader("999999"))
        .get();

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void userIdDoesNotExistTest() throws Exception {
    stubForBody(fixture("fixture/soap/SPIRE_LICENCES/userIdDoesNotExist.xml"));

    Response response = get(LICENCES_URL + "123456");

    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void unhandledErrorTest() throws Exception {
    stubForBody(fixture("fixture/soap/SPIRE_LICENCES/unhandledError.xml"));

    Response response = get(LICENCES_URL + "123456");

    assertThat(response.getStatus()).isEqualTo(500);
  }

  @Test
  public void filterLicencesByRefExistsTest() throws Exception {
    stubForBody(fixture("fixture/soap/SPIRE_LICENCES/singleLicence.xml"));

    Response response = get(LICENCES_URL + "123456", "licenceReference", "REF-123");

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getHeaderString("Content-Type")).isEqualTo("application/json");

    List<LicenceView> licences = Arrays.asList(MAPPER.readValue(response.readEntity(String.class), LicenceView[].class));
    assertThat(licences).hasSize(1);
    assertLicenceViewA(licences.get(0));
  }

  @Test
  public void filterLicencesByRefExistsTooManyLicencesTest() throws Exception {
    stubForBody(fixture("fixture/soap/SPIRE_LICENCES/multipleLicences.xml"));

    Response response = get(LICENCES_URL + "123456", "licenceReference", "REF-123");

    assertThat(response.getStatus()).isEqualTo(400);
    assertThat(response.getHeaderString("Content-Type")).isEqualTo("application/json");

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "400");
    assertThat(map).containsEntry("message", "Too many results from spire client, expected 1 but got 3");
  }

  @Test
  public void filterLicencesByRefDoesNotExistTest() throws Exception {
    stubForBody(fixture("fixture/soap/SPIRE_LICENCES/noLicences.xml"));

    Response response = get(LICENCES_URL + "123456", "licenceReference", "REF-999");

    assertThat(response.getStatus()).isEqualTo(404);
    assertThat(response.getHeaderString("Content-Type")).isEqualTo("application/json");

    Map<String, String> map = Util.getResponseMap(response);
    assertThat(map).hasSize(2);
    assertThat(map).containsEntry("code", "404");
    assertThat(map).containsEntry("message", "No licence with reference REF-999 found for userId 123456");
  }

  @Test
  public void filterLicencesByTypeExistsTest() throws Exception {
    stubForBody(fixture("fixture/soap/SPIRE_LICENCES/singleLicence.xml"));

    Response response = get(LICENCES_URL + "123456", "type", "SIEL");

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getHeaderString("Content-Type")).isEqualTo("application/json");

    List<LicenceView> licences = Arrays.asList(MAPPER.readValue(response.readEntity(String.class), LicenceView[].class));
    assertThat(licences).hasSize(1);
    assertLicenceViewA(licences.get(0));
  }

  @Test
  public void filterLicencesByTypeDoesNotExistTest() throws Exception {
    stubForBody(fixture("fixture/soap/SPIRE_LICENCES/noLicences.xml"));

    Response response = get(LICENCES_URL + "123456", "type", "SIEL");

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getHeaderString("Content-Type")).isEqualTo("application/json");

    List<LicenceView> licences = Arrays.asList(MAPPER.readValue(response.readEntity(String.class), LicenceView[].class));
    assertThat(licences).isEmpty();
  }
}

package uk.gov.bis.lite.permissions.service;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.common.spire.client.SpireClientConfig;
import uk.gov.bis.lite.common.spire.client.SpireRequestConfig;
import uk.gov.bis.lite.common.spire.client.parser.ReferenceParser;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.spire.OgelErrorNodeErrorHandler;
import uk.gov.bis.lite.permissions.spire.SpireReferenceClient;


public class OgelServiceTest {

  private static String USER_ID_SUCCESS = "USER_ID_SUCCESS";
  private static String USER_ID_SOAP_FAULT = "USER_ID_SOAP_FAULT";
  private static String USER_ID_ERROR = "USER_ID_ERROR";
  private static String USER_ID_USER_LACKS_PRIVILEGES = "USER_ID_USER_LACKS_PRIVILEGES";
  private static String USER_ID_BLACKLISTED = "USER_ID_BLACKLISTED";
  private static String USER_ID_SITE_ALREADY_REGISTERED = "USER_ID_SITE_ALREADY_REGISTERED";
  private static String USER_ID_INVALID_OGEL_REF = "USER_ID_INVALID_OGEL_REF";

  @ClassRule
  public static WireMockClassRule wireMockClassRule = new WireMockClassRule(options().dynamicPort());

  private static OgelService ogelService;
  private static ProcessSubmissionServiceImpl.Origin OGEL_CREATE = ProcessSubmissionServiceImpl.Origin.OGEL_CREATE;

  @BeforeClass
  public static void before() {
    configureFor(wireMockClassRule.port());

    String mockSpireUrl = "http://localhost:" + wireMockClassRule.port() + "/spire/fox/ispire/";
    SpireReferenceClient client = provideSpireCreateOgelAppClient("username", "password", mockSpireUrl);
    ogelService = new OgelServiceImpl(client);

    // Initialise Wiremock stubs
    initStubs();
  }

  @Test
  public void testSuccess() throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission(USER_ID_SUCCESS);
    assertThat(ogelService.createOgel(sub)).isPresent().contains(Util.SPIRE_REF);
    assertThat(sub.hasFailEvent()).isFalse();
  }

  @Test
  public void testSoapError() throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission(USER_ID_ERROR);
    assertThat(ogelService.createOgel(sub)).isNotPresent();
    assertThat(sub.hasFailEvent()).isTrue();
    assertThat(sub.getFailEvent().getFailReason()).isEqualTo(Util.ENDPOINT_ERROR);
    assertThat(sub.getFailEvent().getOrigin()).isEqualTo(OGEL_CREATE);
  }

  @Test
  public void testUserLacksPrivileges() throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission(USER_ID_USER_LACKS_PRIVILEGES);
    assertThat(ogelService.createOgel(sub)).isNotPresent();
    assertThat(sub.hasFailEvent()).isTrue();
    assertThat(sub.getFailEvent().getFailReason()).isEqualTo(Util.PERMISSION_DENIED);
    assertThat(sub.getFailEvent().getOrigin()).isEqualTo(OGEL_CREATE);
  }

  @Test
  public void testUserSiteAlreadyRegistered() throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission(USER_ID_SITE_ALREADY_REGISTERED);
    assertThat(ogelService.createOgel(sub)).isNotPresent();
    assertThat(sub.hasFailEvent()).isTrue();
    assertThat(sub.getFailEvent().getFailReason()).isEqualTo(Util.SITE_ALREADY_REGISTERED);
    assertThat(sub.getFailEvent().getOrigin()).isEqualTo(OGEL_CREATE);
  }

  @Test
  public void testUserBlacklisted() throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission(USER_ID_BLACKLISTED);
    assertThat(ogelService.createOgel(sub)).isNotPresent();
    assertThat(sub.hasFailEvent()).isTrue();
    assertThat(sub.getFailEvent().getFailReason()).isEqualTo(Util.BLACKLISTED);
    assertThat(sub.getFailEvent().getOrigin()).isEqualTo(OGEL_CREATE);
  }

  @Test
  public void testInvalidOgelRef() throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission(USER_ID_INVALID_OGEL_REF);
    assertThat(ogelService.createOgel(sub)).isNotPresent();
    assertThat(sub.hasFailEvent()).isTrue();
    assertThat(sub.getFailEvent().getFailReason()).isEqualTo(OgelSubmission.FailReason.ENDPOINT_ERROR);
    assertThat(sub.getFailEvent().getOrigin()).isEqualTo(OGEL_CREATE);
  }

  @Test
  public void testSoapFault() throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission();
    sub.setUserId(USER_ID_SOAP_FAULT);
    assertThat(ogelService.createOgel(sub)).isNotPresent();
    assertThat(sub.hasFailEvent()).isTrue();
    assertThat(sub.getFailEvent().getFailReason()).isEqualTo(Util.UNCLASSIFIED);
    assertThat(sub.getFailEvent().getOrigin()).isEqualTo(OGEL_CREATE);
  }

  private static SpireReferenceClient provideSpireCreateOgelAppClient(String userName, String password, String url) {
    return new SpireReferenceClient(
        new ReferenceParser("REGISTRATION_REF"),
        new SpireClientConfig(userName, password, url),
        new SpireRequestConfig("SPIRE_CREATE_OGEL_APP", "OGEL_DETAILS", false),
        new OgelErrorNodeErrorHandler());
  }

  private static void initStubs() {

    String OGEL_URL = "/spire/fox/ispire/SPIRE_CREATE_OGEL_APP";
    String PATH = "fixture/soap/";
    String CONTENT_TYPE = "Content-Type";
    String TEXT_XML = "text/xml";

    // Success
    stubFor(post(urlEqualTo(OGEL_URL))
        .withRequestBody(containing(USER_ID_SUCCESS))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(CONTENT_TYPE, TEXT_XML)
            .withBody(fixture(PATH + "createOgelSuccess.xml"))));

    // Error
    stubFor(post(urlEqualTo(OGEL_URL))
        .withRequestBody(containing(USER_ID_ERROR))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(CONTENT_TYPE, TEXT_XML)
            .withBody(fixture(PATH + "error.xml"))));

    // Soap Fault Stub
    stubFor(post(urlEqualTo(OGEL_URL))
        .withRequestBody(containing(USER_ID_SOAP_FAULT))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(CONTENT_TYPE, TEXT_XML)
            .withBody(fixture(PATH + "soapFault.xml"))));

    // User lacks privileges
    stubFor(post(urlEqualTo(OGEL_URL))
        .withRequestBody(containing(USER_ID_USER_LACKS_PRIVILEGES))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(CONTENT_TYPE, TEXT_XML)
            .withBody(fixture(PATH + "userLacksPrivileges.xml"))));

    // Site already registered
    stubFor(post(urlEqualTo(OGEL_URL))
        .withRequestBody(containing(USER_ID_SITE_ALREADY_REGISTERED))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(CONTENT_TYPE, TEXT_XML)
            .withBody(fixture(PATH + "siteAlreadyRegistered.xml"))));

    // Blacklisted
    stubFor(post(urlEqualTo(OGEL_URL))
        .withRequestBody(containing(USER_ID_BLACKLISTED))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(CONTENT_TYPE, TEXT_XML)
            .withBody(fixture(PATH + "blacklisted.xml"))));

    // Invalid ogel ref
    stubFor(post(urlEqualTo(OGEL_URL))
        .withRequestBody(containing(USER_ID_INVALID_OGEL_REF))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(CONTENT_TYPE, TEXT_XML)
            .withBody(fixture(PATH + "invalidOgelRef.xml"))));

  }
}

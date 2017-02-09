package uk.gov.bis.lite.permissions.service;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.common.spire.client.SpireClientConfig;
import uk.gov.bis.lite.common.spire.client.SpireRequestConfig;
import uk.gov.bis.lite.common.spire.client.parser.ReferenceParser;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.mocks.FailServiceMock;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.spire.OgelErrorNodeErrorHandler;
import uk.gov.bis.lite.permissions.spire.SpireReferenceClient;


public class OgelServiceTest {

  private String USER_ID_SUCCESS = "USER_ID_SUCCESS";
  private String USER_ID_SOAP_FAULT = "USER_ID_SOAP_FAULT";
  private String USER_ID_ERROR = "USER_ID_ERROR";
  private String USER_ID_USER_LACKS_PRIVILEGES = "USER_ID_USER_LACKS_PRIVILEGES";
  private String USER_ID_BLACKLISTED = "USER_ID_BLACKLISTED";
  private String USER_ID_SITE_ALREADY_REGISTERED = "USER_ID_SITE_ALREADY_REGISTERED";

  private int PORT = 8080;
  private String MOCK_SPIRE_URL = "http://localhost:" + PORT + "/spireuat/fox/ispire/";

  @Rule
  public WireMockRule wiremockRule = new WireMockRule(PORT);

  private OgelService ogelService;
  private FailServiceMock failService;

  @Before
  public void before() {
    SpireReferenceClient client = provideSpireCreateOgelAppClient("username", "password", MOCK_SPIRE_URL);
    failService = new FailServiceMock();
    ogelService = new OgelServiceImpl(client, failService);

    // Initialise Wiremock stubs
    initStubs();
  }

  @Test
  public void testSuccess() throws Exception {
    resetMockFailService();
    OgelSubmission sub = Util.getMockOgelSubmission(USER_ID_SUCCESS);
    assertThat(ogelService.createOgel(sub)).isPresent().contains(Util.SPIRE_REF);;
    assertEquals(0, failServiceCount());
    assertEquals(null, failServiceLastFailReason());
    assertEquals(null, failServiceLastOrigin());
  }

  @Test
  public void testSoapError() throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission(USER_ID_ERROR);
    assertThat(ogelService.createOgel(sub)).isNotPresent();
    assertEquals(1, failServiceCount());
    assertEquals(CallbackView.FailReason.ENDPOINT_ERROR, failServiceLastFailReason());
    assertEquals(FailServiceImpl.Origin.OGEL_CREATE, failServiceLastOrigin());
  }

  @Test
  public void testUserLacksPrivileges() throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission(USER_ID_USER_LACKS_PRIVILEGES);
    assertThat(ogelService.createOgel(sub)).isNotPresent();
    assertEquals(1, failServiceCount());
    assertEquals(CallbackView.FailReason.PERMISSION_DENIED, failServiceLastFailReason());
    assertEquals(FailServiceImpl.Origin.OGEL_CREATE, failServiceLastOrigin());
  }

  @Test
  public void testUserSiteAlreadyRegistered() throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission(USER_ID_SITE_ALREADY_REGISTERED);
    assertThat(ogelService.createOgel(sub)).isNotPresent();
    assertEquals(1, failServiceCount());
    assertEquals(CallbackView.FailReason.SITE_ALREADY_REGISTERED, failServiceLastFailReason());
    assertEquals(FailServiceImpl.Origin.OGEL_CREATE, failServiceLastOrigin());
  }

  @Test
  public void testUserBlacklisted() throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission(USER_ID_BLACKLISTED);
    assertThat(ogelService.createOgel(sub)).isNotPresent();
    assertEquals(1, failServiceCount());
    assertEquals(CallbackView.FailReason.BLACKLISTED, failServiceLastFailReason());
    assertEquals(FailServiceImpl.Origin.OGEL_CREATE, failServiceLastOrigin());
  }

  @Test
  public void testSoapFault() throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission();
    sub.setUserId(USER_ID_SOAP_FAULT);
    assertThat(ogelService.createOgel(sub)).isNotPresent();
    assertEquals(1, failServiceCount());
    assertEquals(CallbackView.FailReason.UNCLASSIFIED, failServiceLastFailReason());
    assertEquals(FailServiceImpl.Origin.OGEL_CREATE, failServiceLastOrigin());
  }

  private SpireReferenceClient provideSpireCreateOgelAppClient(String userName, String password, String url) {
    return new SpireReferenceClient(
        new ReferenceParser("REGISTRATION_REF"),
        new SpireClientConfig(userName, password, url),
        new SpireRequestConfig("SPIRE_CREATE_OGEL_APP", "OGEL_DETAILS", false),
        new OgelErrorNodeErrorHandler());
  }

  private void initStubs() {

    String OGEL_URL = "/spireuat/fox/ispire/SPIRE_CREATE_OGEL_APP";
    String PATH = "fixture/soap/";
    String CONTENT_TYPE = "Content-Type";
    String TEXT_XML = "text/xml";

    // Success
    wiremockRule.stubFor(post(urlEqualTo(OGEL_URL))
        .withRequestBody(containing(USER_ID_SUCCESS))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(CONTENT_TYPE, TEXT_XML)
            .withBody(fixture(PATH + "createOgelSuccess.xml"))));

    // Error
    wiremockRule.stubFor(post(urlEqualTo(OGEL_URL))
        .withRequestBody(containing(USER_ID_ERROR))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(CONTENT_TYPE, TEXT_XML)
            .withBody(fixture(PATH + "error.xml"))));

    // Soap Fault Stub
    wiremockRule.stubFor(post(urlEqualTo(OGEL_URL))
        .withRequestBody(containing(USER_ID_SOAP_FAULT))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(CONTENT_TYPE, TEXT_XML)
            .withBody(fixture(PATH + "soapFault.xml"))));

    // User lacks privileges
    wiremockRule.stubFor(post(urlEqualTo(OGEL_URL))
        .withRequestBody(containing(USER_ID_USER_LACKS_PRIVILEGES))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(CONTENT_TYPE, TEXT_XML)
            .withBody(fixture(PATH + "userLacksPrivileges.xml"))));

    // Site already registered
    wiremockRule.stubFor(post(urlEqualTo(OGEL_URL))
        .withRequestBody(containing(USER_ID_SITE_ALREADY_REGISTERED))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(CONTENT_TYPE, TEXT_XML)
            .withBody(fixture(PATH + "siteAlreadyRegistered.xml"))));

    // Blacklisted
    wiremockRule.stubFor(post(urlEqualTo(OGEL_URL))
        .withRequestBody(containing(USER_ID_BLACKLISTED))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader(CONTENT_TYPE, TEXT_XML)
            .withBody(fixture(PATH + "blacklisted.xml"))));

  }

  private void resetMockFailService() {
    failService.resetAll();
  }

  private int failServiceCount() {
    return failService.getFailServiceCallCount();
  }

  private CallbackView.FailReason failServiceLastFailReason() {
    return failService.getLastFailReason();
  }

  private FailServiceImpl.Origin failServiceLastOrigin() {
    return failService.getLastOrigin();
  }
}

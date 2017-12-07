package uk.gov.bis.lite.permissions.service;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.common.jwt.LiteJwtConfig;
import uk.gov.bis.lite.common.jwt.LiteJwtUserHelper;
import uk.gov.bis.lite.customer.api.param.CustomerParam;
import uk.gov.bis.lite.customer.api.param.UserRoleParam;
import uk.gov.bis.lite.permissions.JwtTestHelper;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Tests CustomerService with external endpoints mocked.
 * Tests FailService dependency is called correctly
 */
public class CustomerServiceTest {

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new CreateSiteEndpoint())
      .addResource(new CreateCustomerEndpoint())
      .addResource(new UserRoleEndpoint()).build();

  private static final String CUSTOMER_ID = "CUSTOMER_ID";
  private static final String SITE_ID = "SITE_ID";
  private static final String SUCCESS = "SUCCESS";
  private static final String FORBIDDEN = "FORBIDDEN";
  private static final String BAD_REQUEST = "BAD_REQUEST";
  private static final String JWT_SHARED_SECRET = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";

  private static String createSiteMode = SUCCESS;
  private static String createCustomerMode = SUCCESS;
  private static String updateUserRoleMode = SUCCESS;

  private CustomerService customerService;

  @Before
  public void before() {
    LiteJwtUserHelper liteJwtUserHelper = new LiteJwtUserHelper(new LiteJwtConfig(JWT_SHARED_SECRET, "lite-permissions-service"));
    customerService = new CustomerServiceImpl(resources.client(), "/", liteJwtUserHelper);
  }

  @Test
  public void testUserRoleSuccess() throws Exception {
    // Setup
    userRoleMode(SUCCESS);

    OgelSubmission sub = getUserRoleOgelSubmission();

    // Test
    assertThat(customerService.updateUserRole(sub)).isTrue();
    assertThat(sub.hasFailEvent()).isFalse();
  }

  @Test
  public void testUserRoleBadRequest() throws Exception {
    // Setup
    userRoleMode(BAD_REQUEST);

    OgelSubmission sub = getUserRoleOgelSubmission();

    // Test
    assertThat(customerService.updateUserRole(sub)).isFalse();
    assertThat(sub.hasFailEvent()).isTrue();
    assertThat(sub.getFailEvent().getFailReason()).isEqualTo(Util.ENDPOINT_ERROR);
    assertThat(sub.getFailEvent().getOrigin()).isEqualTo(Util.ORIGIN_USER_ROLE);
  }

  @Test
  public void testCustomerSuccess() throws Exception {
    // Setup
    customerMode(SUCCESS);

    OgelSubmission sub = getCustomerOgelSubmission();

    // Test
    assertThat(customerService.getOrCreateCustomer(sub)).isPresent().contains(CUSTOMER_ID);
    assertThat(sub.hasFailEvent()).isFalse();
  }

  @Test
  public void testCustomerBadRequest() throws Exception {
    // Setup
    customerMode(BAD_REQUEST);

    OgelSubmission sub = getCustomerOgelSubmission();

    // Test
    assertThat(customerService.getOrCreateCustomer(sub)).isNotPresent();
    assertThat(sub.hasFailEvent()).isTrue();
    assertThat(sub.getFailEvent().getFailReason()).isEqualTo(Util.ENDPOINT_ERROR);
    assertThat(sub.getFailEvent().getOrigin()).isEqualTo(Util.ORIGIN_CUSTOMER);
  }

  @Test
  public void testSiteSuccess() throws Exception {
    // Setup
    siteMode(SUCCESS);

    OgelSubmission sub = getSiteOgelSubmission();

    // Test
    assertThat(customerService.createSite(sub)).isPresent().contains(SITE_ID);
    assertThat(sub.hasFailEvent()).isFalse();
  }

  @Test
  public void testSiteForbidden() throws Exception {
    // Setup
    siteMode(FORBIDDEN);

    OgelSubmission sub = getSiteOgelSubmission();

    // Test
    assertThat(customerService.createSite(sub)).isNotPresent();
    assertThat(sub.hasFailEvent()).isTrue();
    assertThat(sub.getFailEvent().getFailReason()).isEqualTo(Util.PERMISSION_DENIED);
    assertThat(sub.getFailEvent().getOrigin()).isEqualTo(Util.ORIGIN_SITE);
  }

  @Test
  public void testSiteBadRequest() throws Exception {
    // Setup
    siteMode(BAD_REQUEST);

    OgelSubmission sub = getSiteOgelSubmission();

    // Test
    assertThat(customerService.createSite(sub)).isNotPresent();
    assertThat(sub.hasFailEvent()).isTrue();
    assertThat(sub.getFailEvent().getFailReason()).isEqualTo(Util.ENDPOINT_ERROR);
    assertThat(sub.getFailEvent().getOrigin()).isEqualTo(Util.ORIGIN_SITE);
  }

  /**
   * Mocked Endpoints
   */

  @Path("/")
  public static class CreateCustomerEndpoint {

    @POST
    @Path("/create-customer")
    public Response createCustomer(CustomerParam param) {
      switch (createCustomerMode) {
        case SUCCESS:
          return Response.ok(fixture("fixture/createCustomerCustomerView.json"), MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8")).build();
        case BAD_REQUEST:
          return Response.status(Response.Status.BAD_REQUEST).build();
        default:
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    }
  }

  @Path("/")
  public static class CreateSiteEndpoint {

    @POST
    @Path("/customer-sites/{customerId}")
    public Response createSite(@PathParam("customerId") String customerId) {
      switch (createSiteMode) {
        case SUCCESS:
          return Response.ok(fixture("fixture/createSiteSiteView.json"), MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8")).build();
        case FORBIDDEN:
          return Response.status(Response.Status.FORBIDDEN).build();
        case BAD_REQUEST:
          return Response.status(Response.Status.BAD_REQUEST).build();
        default:
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    }
  }

  @Path("/")
  public static class UserRoleEndpoint {

    @POST
    @Path("/user-roles/user/{userId}/site/{siteRef}")
    public Response userRole(@NotNull @PathParam("userId") String userId,
                             @NotNull @PathParam("siteRef") String siteRef,
                             UserRoleParam param) {
      switch (updateUserRoleMode) {
        case SUCCESS:
          return Response.ok().build();
        case BAD_REQUEST:
          return Response.status(Response.Status.BAD_REQUEST)
              .entity(ImmutableMap.of("code", Response.Status.BAD_REQUEST, "message", "error"))
              .type(MediaType.APPLICATION_JSON_TYPE)
              .build();
        default:
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    }
  }

  /**
   * Util Methods
   */

  private void customerMode(String arg) {
    createCustomerMode = arg;
  }

  private void siteMode(String arg) {
    createSiteMode = arg;
  }

  private void userRoleMode(String arg) {
    updateUserRoleMode = arg;
  }

  private OgelSubmission getUserRoleOgelSubmission() {
    String newSiteJson = fixture("fixture/registerForRoleUpdate.json");
    OgelSubmission sub = new OgelSubmission("userId", "ogelType");
    sub.setSiteRef(SITE_ID);
    sub.setLiteJwtUser(JwtTestHelper.LITE_JWT_USER);
    sub.setJson(newSiteJson);
    return sub;
  }

  private OgelSubmission getCustomerOgelSubmission() {
    String newSiteJson = fixture("fixture/createCustomer.json");
    OgelSubmission sub = new OgelSubmission("userId", "ogelType");
    sub.setJson(newSiteJson);
    sub.setLiteJwtUser(JwtTestHelper.LITE_JWT_USER);
    return sub;
  }

  private OgelSubmission getSiteOgelSubmission() {
    String newSiteJson = fixture("fixture/createSite.json");
    OgelSubmission sub = new OgelSubmission("userId", "ogelType");
    sub.setCustomerRef(CUSTOMER_ID);
    sub.setJson(newSiteJson);
    sub.setLiteJwtUser(JwtTestHelper.LITE_JWT_USER);
    return sub;
  }

}

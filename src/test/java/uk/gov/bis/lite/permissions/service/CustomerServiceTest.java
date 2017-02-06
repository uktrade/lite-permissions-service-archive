package uk.gov.bis.lite.permissions.service;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.customer.api.param.CustomerParam;
import uk.gov.bis.lite.customer.api.param.UserRoleParam;
import uk.gov.bis.lite.permissions.mocks.FailServiceMock;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Tests CustomerService with external endpoints mocked.
 * Tests mocked FailService is called correctly
 */
public class CustomerServiceTest {

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new CreateSiteEndpoint())
      .addResource(new CreateCustomerEndpoint())
      .addResource(new UserRoleEndpoint()).build();

  private CustomerService customerService;
  private FailServiceMock failService;

  private static String CUSTOMER_ID = "CUSTOMER_ID";
  private static String SITE_ID = "SITE_ID";

  private static String SUCCESS = "SUCCESS";
  private static String FORBIDDEN = "FORBIDDEN";
  private static String BAD_REQUEST = "BAD_REQUEST";
  private static String createSiteMode = SUCCESS;
  private static String createCustomerMode = SUCCESS;
  private static String updateUserRoleMode = SUCCESS;

  @Before
  public void before() {
    failService = new FailServiceMock();
    customerService = new CustomerServiceImpl(resources.client(), failService, "/");
  }

  @Test
  public void testUserRoleSuccess() throws Exception {
    // Setup
    resetFailServiceCount();
    userRoleMode(SUCCESS);

    // Test
    assertThat(customerService.updateUserRole(getUserRoleOgelSubmission())).isTrue();
    assertEquals(0, failServiceCount());
  }

  @Test
  public void testUserRoleBadRequest() throws Exception {
    // Setup
    resetFailServiceCount();
    userRoleMode(BAD_REQUEST);

    // Test
    assertThat(customerService.updateUserRole(getUserRoleOgelSubmission())).isFalse();
    assertEquals(1, failServiceCount());
  }

  @Test
  public void testCustomerSuccess() throws Exception {
    // Setup
    resetFailServiceCount();
    customerMode(SUCCESS);

    // Test
    assertThat(customerService.getOrCreateCustomer(getCustomerOgelSubmission())).isPresent().contains(CUSTOMER_ID);
    assertEquals(0, failServiceCount());
  }

  @Test
  public void testCustomerBadRequest() throws Exception {
    // Setup
    resetFailServiceCount();
    customerMode(BAD_REQUEST);

    // Test
    assertThat(customerService.getOrCreateCustomer(getCustomerOgelSubmission())).isNotPresent();
    assertEquals(1, failServiceCount());
  }

  @Test
  public void testSiteSuccess() throws Exception {
    // Setup
    resetFailServiceCount();
    siteMode(SUCCESS);

    // Test
    assertThat(customerService.createSite(getSiteOgelSubmission())).isPresent().contains(SITE_ID);
    assertEquals(0, failServiceCount());
  }

  @Test
  public void testSiteForbidden() throws Exception {
    // Setup
    resetFailServiceCount();
    siteMode(FORBIDDEN);

    // Test
    assertThat(customerService.createSite(getSiteOgelSubmission())).isNotPresent();
    assertEquals(1, failServiceCount());
  }

  @Test
  public void testSiteBadRequest() throws Exception {
    // Setup
    resetFailServiceCount();
    siteMode(BAD_REQUEST);

    // Test
    assertThat(customerService.createSite(getSiteOgelSubmission())).isNotPresent();
    assertEquals(1, failServiceCount());
  }

  /**
   * Mocked Endpoints
   */

  @Path("/")
  public static class CreateCustomerEndpoint {
    @POST
    @Path("/create-customer")
    public Response createCustomer(CustomerParam param) {
      if(createCustomerMode.equals(SUCCESS)) {
        return Response.ok(fixture("fixture/createCustomerCustomerView.json"), MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8")).build();
      }
      if(createCustomerMode.equals(BAD_REQUEST)) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("/")
  public static class CreateSiteEndpoint {
    @POST
    @Path("/customer-sites/{customerId}")
    public Response createSite(@PathParam("customerId") String customerId) {
      if(createSiteMode.equals(SUCCESS)) {
        return Response.ok(fixture("fixture/createSiteSiteView.json"), MediaType.APPLICATION_JSON_TYPE.withCharset("utf-8")).build();
      }
      if(createSiteMode.equals(FORBIDDEN)) {
        return Response.status(Response.Status.FORBIDDEN).build();
      }
      if(createSiteMode.equals(BAD_REQUEST)) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("/")
  public static class UserRoleEndpoint {
    @POST
    @Path("/user-roles/user/{userId}/site/{siteRef}")
    public Response userRole(@NotNull @PathParam("userId") String userId,
                             @NotNull @PathParam("siteRef") String siteRef,
                             UserRoleParam param) {
      if(updateUserRoleMode.equals(SUCCESS)) {
        return Response.ok().build();
      }
      if(updateUserRoleMode.equals(BAD_REQUEST)) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(ImmutableMap.of("code", Response.Status.BAD_REQUEST, "message", "error"))
            .type(MediaType.APPLICATION_JSON_TYPE)
            .build();
      }
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
    sub.setJson(newSiteJson);
    return sub;
  }

  private OgelSubmission getCustomerOgelSubmission() {
    String newSiteJson = fixture("fixture/createCustomer.json");
    OgelSubmission sub = new OgelSubmission("userId", "ogelType");
    sub.setJson(newSiteJson);
    return sub;
  }

  private OgelSubmission getSiteOgelSubmission() {
    String newSiteJson = fixture("fixture/createSite.json");
    OgelSubmission sub = new OgelSubmission("userId", "ogelType");
    sub.setCustomerRef(CUSTOMER_ID);
    sub.setJson(newSiteJson);
    return sub;
  }

  private void resetFailServiceCount() {
    failService.resetFailServiceCallCount();
  }

  private int failServiceCount() {
    return failService.getFailServiceCallCount();
  }

}

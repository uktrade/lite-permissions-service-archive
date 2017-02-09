package uk.gov.bis.lite.permissions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.bis.lite.permissions.Util.getMockOgelSubmission;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.mocks.FailServiceMock;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Tests CallbackService and CallbackView state
 */
public class CallbackServiceTest {

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new CallbackEndpoint()).build();

  private CallbackServiceImpl callbackService;
  private FailServiceMock failService;

  private static String SUCCESS = "SUCCESS";
  private static String FAILED = "FAILED";
  private static String callbackEndpointResult = SUCCESS;

  @Before
  public void before() {
    failService = new FailServiceMock();
    callbackService = new CallbackServiceImpl(resources.client(), failService);
  }

  @Test
  public void testCallbackSuccess() throws Exception {
    // Setup
    resetMockFailService();
    setCallbackEndpointSuccess();

    OgelSubmission sub = getMockOgelSubmission();

    // Test
    assertThat(callbackService.completeCallback(sub)).isEqualTo(true);
    assertThat(sub.isCalledBack()).isEqualTo(true);
    assertEquals(0, failServiceCount());
  }

  @Test
  public void testCallbackFailure() throws Exception {
    // Setup
    resetMockFailService();
    setCallbackEndpointFailed();

    OgelSubmission sub = getMockOgelSubmission();

    // Test
    assertThat(callbackService.completeCallback(getMockOgelSubmission())).isEqualTo(false);
    assertThat(sub.isCalledBack()).isEqualTo(false);
    assertEquals(1, failServiceCount());
    assertEquals(failServiceLastFailReason(), CallbackView.FailReason.UNCLASSIFIED);
  }

  @Test
  public void testCallbackViewSuccess() throws Exception {
    OgelSubmission sub = getMockOgelSubmission();
    CallbackView view = callbackService.getCallbackView(sub);

    assertEquals(view.getStatus(), CallbackView.Status.SUCCESS);
    assertEquals(view.getRegistrationReference(), Util.SPIRE_REF);
    assertEquals(view.getCustomerId(), Util.CUSTOMER_REF);
    assertEquals(view.getSiteId(), Util.SITE_REF);
    assertEquals(view.getRequestId(), Util.SUBMISSION_REF + Util.MOCK_ID);
    assertEquals(view.getFailReason(), null);
  }

  @Test
  public void testCallbackViewNoRegistrationReference() throws Exception {
    OgelSubmission sub = getMockOgelSubmission();
    sub.setSpireRef(null); // removed mocked SpireRef
    CallbackView view = callbackService.getCallbackView(sub);

    assertEquals(view.getStatus(), CallbackView.Status.FAILED);
    assertEquals(view.getRegistrationReference(), null);
    assertEquals(view.getCustomerId(), Util.CUSTOMER_REF);
    assertEquals(view.getSiteId(), Util.SITE_REF);
    assertEquals(view.getRequestId(), Util.SUBMISSION_REF + Util.MOCK_ID);
    assertEquals(view.getFailReason(), null);
  }

  @Test
  public void testCallbackViewNotComplete() throws Exception {
    OgelSubmission sub = getMockOgelSubmission();
    sub.setStatus(OgelSubmission.Status.ACTIVE); // change mocked COMPLETE to ACTIVE

    CallbackView view = callbackService.getCallbackView(sub);

    assertEquals(view.getStatus(), CallbackView.Status.FAILED);
    assertEquals(view.getRegistrationReference(), null);
    assertEquals(view.getCustomerId(), Util.CUSTOMER_REF);
    assertEquals(view.getSiteId(), Util.SITE_REF);
    assertEquals(view.getRequestId(), Util.SUBMISSION_REF + Util.MOCK_ID);
    assertEquals(view.getFailReason(), null);
  }

  @Test
  public void testCallbackViewWithFailReason() throws Exception {
    OgelSubmission sub = Util.getMockOgelSubmission();
    sub.setFailReason(CallbackView.FailReason.PERMISSION_DENIED);
    sub.setSpireRef(null); // removed mocked SpireRef

    CallbackView view = callbackService.getCallbackView(sub);

    assertEquals(view.getStatus(), CallbackView.Status.FAILED);
    assertEquals(view.getRegistrationReference(), null);
    assertEquals(view.getCustomerId(), Util.CUSTOMER_REF);
    assertEquals(view.getSiteId(), Util.SITE_REF);
    assertEquals(view.getRequestId(), Util.SUBMISSION_REF + Util.MOCK_ID);
    assertEquals(view.getFailReason(), CallbackView.FailReason.PERMISSION_DENIED);
  }

  /**
   * Mocked Callback Endpoint
   */

  @Path("/")
  public static class CallbackEndpoint {
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Path(Util.MOCK_CALLBACK_URL)
    public Response callback(CallbackView view) {
      if(callbackEndpointResult.equals(SUCCESS)) {
        return Response.ok().build();
      }
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Util Methods
   */
  private void setCallbackEndpointSuccess() {
    callbackEndpointResult = SUCCESS;
  }

  private void setCallbackEndpointFailed() {
    callbackEndpointResult = FAILED;
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

}
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
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Tests CallbackService and CallbackView state
 */
public class CallbackServiceTest {

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new CallbackEndpoint()).build();

  private static boolean callbackSuccessful = true;

  private CallbackServiceImpl callbackService;

  @Before
  public void before() {
    callbackService = new CallbackServiceImpl(resources.client());
  }

  /**
   * CallbackService tests
   */
  @Test
  public void testCallbackSuccess() throws Exception {

    // Setup
    callbackSuccessful = true;
    OgelSubmission sub = getMockOgelSubmission();

    // Test
    assertThat(callbackService.completeCallback(sub)).isEqualTo(true);
    assertThat(sub.isCalledBack()).isEqualTo(true);
    assertThat(sub.hasFailEvent()).isFalse();
    assertThat(sub.getFailReason()).isNull();
  }

  @Test
  public void testCallbackFailure() throws Exception {

    // Setup
    callbackSuccessful = false;
    OgelSubmission sub = getMockOgelSubmission();

    // Test
    assertThat(callbackService.completeCallback(sub)).isEqualTo(false);
    assertThat(sub.isCalledBack()).isEqualTo(false);
    assertThat(sub.hasFailEvent()).isTrue();
    assertThat(sub.getFailEvent().getFailReason()).isEqualTo(Util.UNCLASSIFIED);
    assertThat(sub.getFailEvent().getOrigin()).isEqualTo(Util.ORIGIN_CALLBACK);
  }

  /**
   * CallbackView tests
   */

  @Test
  public void testCallbackViewSuccess() throws Exception {

    // Setup
    OgelSubmission sub = getMockOgelSubmission();
    CallbackView view = callbackService.getCallbackView(sub);

    // Test
    assertEquals(view.getResult(), CallbackView.Result.SUCCESS);
    assertEquals(view.getRegistrationReference(), Util.SPIRE_REF);
    assertEquals(view.getCustomerId(), Util.CUSTOMER_REF);
    assertEquals(view.getSiteId(), Util.SITE_REF);
    assertEquals(view.getRequestId(), Util.SUBMISSION_REF + Util.MOCK_ID);
  }

  @Test
  public void testCallbackViewNoRegistrationReference() throws Exception {

    // Setup
    OgelSubmission sub = getMockOgelSubmission();
    sub.setSpireRef(null); // removed mocked SpireRef
    CallbackView view = callbackService.getCallbackView(sub);

    // Test
    assertEquals(view.getResult(), CallbackView.Result.FAILED);
    assertEquals(view.getRegistrationReference(), null);
    assertEquals(view.getCustomerId(), Util.CUSTOMER_REF);
    assertEquals(view.getSiteId(), Util.SITE_REF);
    assertEquals(view.getRequestId(), Util.SUBMISSION_REF + Util.MOCK_ID);
  }

  @Test
  public void testCallbackViewNotComplete() throws Exception {

    // Setup
    OgelSubmission sub = getMockOgelSubmission();
    sub.setStatus(Util.STATUS_ACTIVE); // change mocked COMPLETE to ACTIVE

    CallbackView view = callbackService.getCallbackView(sub);

    // Test
    assertEquals(view.getResult(), CallbackView.Result.FAILED);
    assertEquals(view.getRegistrationReference(), null);
    assertEquals(view.getCustomerId(), Util.CUSTOMER_REF);
    assertEquals(view.getSiteId(), Util.SITE_REF);
    assertEquals(view.getRequestId(), Util.SUBMISSION_REF + Util.MOCK_ID);
  }

  @Test
  public void testViewFromFailReason() throws Exception {
    CallbackView view1 = callbackService.getCallbackView(Util.getMockWithFailReason(OgelSubmission.FailReason.PERMISSION_DENIED));
    assertEquals(view1.getResult(), CallbackView.Result.PERMISSION_DENIED);

    CallbackView view2 = callbackService.getCallbackView(Util.getMockWithFailReason(OgelSubmission.FailReason.BLACKLISTED));
    assertEquals(view2.getResult(), CallbackView.Result.BLACKLISTED);

    CallbackView view3 = callbackService.getCallbackView(Util.getMockWithFailReason(OgelSubmission.FailReason.SITE_ALREADY_REGISTERED));
    assertEquals(view3.getResult(), CallbackView.Result.SITE_ALREADY_REGISTERED);

    CallbackView view4 = callbackService.getCallbackView(Util.getMockWithFailReason(OgelSubmission.FailReason.ENDPOINT_ERROR));
    assertEquals(view4.getResult(), CallbackView.Result.FAILED);

    CallbackView view5 = callbackService.getCallbackView(Util.getMockWithFailReason(OgelSubmission.FailReason.UNCLASSIFIED));
    assertEquals(view5.getResult(), CallbackView.Result.FAILED);

    OgelSubmission sub = Util.getMockOgelSubmission();
    sub.setSpireRef(null); // mock for failure

    CallbackView view6 = callbackService.getCallbackView(sub);
    assertEquals(view6.getResult(), CallbackView.Result.FAILED);
  }

  /**
   * Mocked Callback Endpoint
   */
  @Path("/")
  public static class CallbackEndpoint {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(Util.MOCK_CALLBACK_URL)
    public Response callback(CallbackView view) {
      if (callbackSuccessful) {
        return Response.ok().build();
      } else {
        return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
    }

  }

}

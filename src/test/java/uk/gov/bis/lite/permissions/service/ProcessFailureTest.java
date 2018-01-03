package uk.gov.bis.lite.permissions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.mocks.CallbackServiceMock;
import uk.gov.bis.lite.permissions.mocks.CustomerServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelSubmissionDaoMock;
import uk.gov.bis.lite.permissions.model.FailEvent;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

public class ProcessFailureTest {

  private ProcessSubmissionServiceImpl service;

  @Before
  public void before() {

    // maxMinutesRetryAfterFail set to 0 minutes for testRepeatingError test (does not affect other tests)
    service = new ProcessSubmissionServiceImpl(new OgelSubmissionDaoMock(), new CustomerServiceMock(),
        new OgelServiceMock(), new CallbackServiceMock(), 0, 1);
  }

  @Test
  public void testNoFailEvent() throws Exception {
    OgelSubmission sub = Util.getMockActiveOgelSubmission();

    service.updateForProcessFailure(sub);

    assertThat(sub.hasFailEvent()).isFalse();
    assertThat(sub.getStatus()).isEqualTo(OgelSubmission.Status.ACTIVE);
    assertThat(sub.getLastFailMessage()).isNull();
    assertThat(sub.getFailReason()).isNull();
  }

  @Test
  public void testFirstFail() throws Exception {
    OgelSubmission sub = Util.getMockActiveOgelSubmission();
    sub.setFailEvent(new FailEvent(Util.PERMISSION_DENIED, Util.ORIGIN_SITE, Util.ERROR_MESSAGE));
    assertThat(sub.getFirstFail()).isNull();
    service.updateForProcessFailure(sub);
    assertThat(sub.getFirstFail()).isNotNull();
    assertThat(sub.getLastFailMessage()).isNotNull();
  }

  @Test
  public void testPermissionDeniedFailEvent() throws Exception {
    OgelSubmission sub = Util.getMockActiveOgelSubmission();
    sub.setFailEvent(new FailEvent(Util.PERMISSION_DENIED, Util.ORIGIN_SITE, Util.ERROR_MESSAGE));

    service.updateForProcessFailure(sub);

    assertThat(sub.hasFailEvent()).isFalse();
    assertThat(sub.getFailReason()).isEqualTo(Util.PERMISSION_DENIED);
    assertThat(sub.getLastFailMessage()).contains(Util.PERMISSION_DENIED.name());
    assertThat(sub.getLastFailMessage()).contains(Util.ORIGIN_SITE.name());
    assertThat(sub.getLastFailMessage()).contains(Util.ERROR_MESSAGE);
    assertThat(sub.getStatus()).isEqualTo(Util.STATUS_COMPLETE);
  }

  @Test
  public void testBlacklistedFailEvent() throws Exception {
    OgelSubmission sub = Util.getMockActiveOgelSubmission();
    sub.setFailEvent(new FailEvent(Util.BLACKLISTED, Util.ORIGIN_SITE, Util.ERROR_MESSAGE));

    service.updateForProcessFailure(sub);

    assertThat(sub.hasFailEvent()).isFalse();
    assertThat(sub.getFailReason()).isEqualTo(Util.BLACKLISTED);
    assertThat(sub.getLastFailMessage()).contains(Util.BLACKLISTED.name());
    assertThat(sub.getLastFailMessage()).contains(Util.ORIGIN_SITE.name());
    assertThat(sub.getLastFailMessage()).contains(Util.ERROR_MESSAGE);
    assertThat(sub.getStatus()).isEqualTo(Util.STATUS_COMPLETE);
  }

  @Test
  public void testSiteAlreadyRegisteredFailEvent() throws Exception {
    OgelSubmission sub = Util.getMockActiveOgelSubmission();
    sub.setFailEvent(new FailEvent(Util.SITE_ALREADY_REGISTERED, Util.ORIGIN_SITE, Util.ERROR_MESSAGE));

    service.updateForProcessFailure(sub);

    assertThat(sub.hasFailEvent()).isFalse();
    assertThat(sub.getFailReason()).isEqualTo(Util.SITE_ALREADY_REGISTERED);
    assertThat(sub.getLastFailMessage()).contains(Util.SITE_ALREADY_REGISTERED.name());
    assertThat(sub.getLastFailMessage()).contains(Util.ORIGIN_SITE.name());
    assertThat(sub.getLastFailMessage()).contains(Util.ERROR_MESSAGE);
    assertThat(sub.getStatus()).isEqualTo(Util.STATUS_COMPLETE);
  }

  @Test
  public void testRepeatingError() throws Exception {
    OgelSubmission sub = Util.getMockActiveOgelSubmission();
    sub.setFirstFailDateTime();
    sub.setFailEvent(new FailEvent(Util.UNCLASSIFIED, Util.ORIGIN_SITE, Util.ERROR_MESSAGE));

    // maxMinutesRetryAfterFail set to 0 minutes so we expect Status to be set to TERMINATED
    service.updateForProcessFailure(sub);

    assertThat(sub.hasFailEvent()).isFalse();
    assertThat(sub.getStatus()).isEqualTo(Util.STATUS_COMPLETE);
  }

  @Test
  public void testRepeatingCallbackError() throws Exception {
    OgelSubmission sub = Util.getMockCallbackOgelSubmission();
    assertEquals(sub.getCallBackFailCount(), 0);

    // maxCallbackFailCount set to 1 so we expect Status to be set to TERMINATED after 2 iterations

    sub.setFailEvent(new FailEvent(Util.UNCLASSIFIED, Util.ORIGIN_CALLBACK, Util.ERROR_MESSAGE));
    service.updateForCallbackFailure(sub);
    assertThat(sub.getStatus()).isEqualTo(Util.STATUS_COMPLETE);
    assertEquals(1, sub.getCallBackFailCount());

    sub.setFailEvent(new FailEvent(Util.UNCLASSIFIED, Util.ORIGIN_CALLBACK, Util.ERROR_MESSAGE));
    service.updateForCallbackFailure(sub);
    assertThat(sub.getStatus()).isEqualTo(Util.STATUS_COMPLETE);
    assertEquals(2, sub.getCallBackFailCount());

    sub.setFailEvent(new FailEvent(Util.UNCLASSIFIED, Util.ORIGIN_CALLBACK, Util.ERROR_MESSAGE));
    service.updateForCallbackFailure(sub);
    assertThat(sub.getStatus()).isEqualTo(Util.STATUS_TERMINATED);
    assertEquals(2, sub.getCallBackFailCount());

  }

}

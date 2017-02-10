package uk.gov.bis.lite.permissions.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.mocks.CallbackServiceMock;
import uk.gov.bis.lite.permissions.mocks.CustomerServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelSubmissionDaoMock;
import uk.gov.bis.lite.permissions.model.FailEvent;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

public class ProcessFailureTest {

  private ProcessSubmissionServiceImpl service;
  private String ERROR_MESSAGE = "ERROR_MESSAGE";

  private CallbackView.FailReason PERMISSION_DENIED = CallbackView.FailReason.PERMISSION_DENIED;
  private CallbackView.FailReason BLACKLISTED = CallbackView.FailReason.BLACKLISTED;
  private CallbackView.FailReason SITE_ALREADY_REGISTERED = CallbackView.FailReason.SITE_ALREADY_REGISTERED;
  private CallbackView.FailReason ENDPOINT_ERROR = CallbackView.FailReason.ENDPOINT_ERROR;
  private CallbackView.FailReason UNCLASSIFIED = CallbackView.FailReason.UNCLASSIFIED;

  private ProcessSubmissionServiceImpl.Origin SITE = ProcessSubmissionServiceImpl.Origin.SITE;

  private OgelSubmission.Status ACTIVE = OgelSubmission.Status.ACTIVE;
  private OgelSubmission.Status COMPLETE = OgelSubmission.Status.COMPLETE;
  private OgelSubmission.Status TERMINATED = OgelSubmission.Status.TERMINATED;

  @Before
  public void before() {

    // maxMinutesRetryAfterFail set to 0 minutes for testRepeatingError test (does not affect other tests)
    service = new ProcessSubmissionServiceImpl(new OgelSubmissionDaoMock(), new CustomerServiceMock(),
        new OgelServiceMock(), new CallbackServiceMock(), 0);
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
    sub.setFailEvent(new FailEvent(PERMISSION_DENIED, SITE, ERROR_MESSAGE));
    assertThat(sub.getFirstFailDateTime()).isNull();
    service.updateForProcessFailure(sub);
    assertThat(sub.getFirstFailDateTime()).isNotNull();
    assertThat(sub.getLastFailMessage()).isNotNull();
  }

  @Test
  public void testPermissionDeniedFailEvent() throws Exception {
    OgelSubmission sub = Util.getMockActiveOgelSubmission();
    sub.setFailEvent(new FailEvent(PERMISSION_DENIED, SITE, ERROR_MESSAGE));

    service.updateForProcessFailure(sub);

    assertThat(sub.hasFailEvent()).isFalse();
    assertThat(sub.getFailReason()).isEqualTo(PERMISSION_DENIED);
    assertThat(sub.getLastFailMessage()).contains(PERMISSION_DENIED.name());
    assertThat(sub.getLastFailMessage()).contains(SITE.name());
    assertThat(sub.getLastFailMessage()).contains(ERROR_MESSAGE);
    assertThat(sub.getStatus()).isEqualTo(COMPLETE);
  }

  @Test
  public void testBlacklistedFailEvent() throws Exception {
    OgelSubmission sub = Util.getMockActiveOgelSubmission();
    sub.setFailEvent(new FailEvent(BLACKLISTED, SITE, ERROR_MESSAGE));

    service.updateForProcessFailure(sub);

    assertThat(sub.hasFailEvent()).isFalse();
    assertThat(sub.getFailReason()).isEqualTo(BLACKLISTED);
    assertThat(sub.getLastFailMessage()).contains(BLACKLISTED.name());
    assertThat(sub.getLastFailMessage()).contains(SITE.name());
    assertThat(sub.getLastFailMessage()).contains(ERROR_MESSAGE);
    assertThat(sub.getStatus()).isEqualTo(COMPLETE);
  }

  @Test
  public void testSiteAlreadyRegisteredFailEvent() throws Exception {
    OgelSubmission sub = Util.getMockActiveOgelSubmission();
    sub.setFailEvent(new FailEvent(SITE_ALREADY_REGISTERED, SITE, ERROR_MESSAGE));

    service.updateForProcessFailure(sub);

    assertThat(sub.hasFailEvent()).isFalse();
    assertThat(sub.getFailReason()).isEqualTo(SITE_ALREADY_REGISTERED);
    assertThat(sub.getLastFailMessage()).contains(SITE_ALREADY_REGISTERED.name());
    assertThat(sub.getLastFailMessage()).contains(SITE.name());
    assertThat(sub.getLastFailMessage()).contains(ERROR_MESSAGE);
    assertThat(sub.getStatus()).isEqualTo(COMPLETE);
  }

  @Test
  public void testRepeatingError() throws Exception {
    OgelSubmission sub = Util.getMockActiveOgelSubmission();
    sub.setFirstFailDateTime();
    sub.setFailEvent(new FailEvent(UNCLASSIFIED, SITE, ERROR_MESSAGE));

    // maxMinutesRetryAfterFail set to 0 minutes so we expect Status to be set to TERMINATED
    service.updateForProcessFailure(sub);

    assertThat(sub.hasFailEvent()).isFalse();
    assertThat(sub.getStatus()).isEqualTo(TERMINATED);
  }

}

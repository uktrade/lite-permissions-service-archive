package uk.gov.bis.lite.permissions.service;

import static java.time.temporal.ChronoUnit.MINUTES;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.time.LocalDateTime;

@Singleton
class FailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FailService.class);

  private OgelSubmissionDao submissionDao;
  private int maxMinutesRetryAfterFail;

  /**
   * Origin of call to 'fail': CUSTOMER, SITE, USER_ROLE, OGEL_CREATE, CALLBACK
   */
  enum Origin {
    CUSTOMER, SITE, USER_ROLE, OGEL_CREATE, CALLBACK;
  }

  @Inject
  public FailService(OgelSubmissionDao submissionDao,
                     @Named("maxMinutesRetryAfterFail") int maxMinutesRetryAfterFail) {
    this.submissionDao = submissionDao;
    this.maxMinutesRetryAfterFail = maxMinutesRetryAfterFail;
  }

  void fail(OgelSubmission sub, CallbackView.FailReason failReason, FailService.Origin origin) {
    doFailUpdate(sub, failReason, origin, null);
  }

  void fail(OgelSubmission sub, CallbackView.FailReason failReason, FailService.Origin origin, String message) {
    doFailUpdate(sub, failReason, origin, message);
  }

  /**
   * Updates OgelSubmission with error details, sets OgelSubmission status to ERROR when appropriate
   */
  private void doFailUpdate(OgelSubmission sub, CallbackView.FailReason failReason, Origin origin, String message) {
    String failMessage = createFailMessage(failReason, origin, message);
    LOGGER.error(failMessage);

    if (!sub.hasFail()) {
      sub.setFirstFailDateTime(); // Set first fail
    } else {
      // Check for repeating error
      if (sub.getFirstFailDateTime().isBefore(LocalDateTime.now().minus(maxMinutesRetryAfterFail, MINUTES))) {
        LOGGER.info("Terminal failure setting status to ERROR [" + sub.getRequestId() + "]");
        sub.updateStatusToError();
      }
    }

    sub.setFailReason(failReason);
    sub.setLastFailMessage(failMessage);

    // Check if we have a terminal fail reason
    if (failReason == CallbackView.FailReason.BLACKLISTED || failReason == CallbackView.FailReason.PERMISSION_DENIED) {
      sub.updateStatusToError();
    }

    submissionDao.update(sub);
  }

  private String createFailMessage(CallbackView.FailReason failReason, Origin origin, String message) {
    String failMessage = "FailReason[" + failReason.name() + "] Origin[" + origin.name() + "]";
    if (!StringUtils.isBlank(message)) {
      failMessage = failMessage + " - " + message;
    }
    return failMessage;
  }
}

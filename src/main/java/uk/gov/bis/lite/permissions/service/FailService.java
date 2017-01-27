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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Singleton
class FailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FailService.class);

  private OgelSubmissionDao submissionDao;
  private int maxMinutesRetryAfterFail;

  private static final Set<CallbackView.FailReason> terminalFailReasons = new HashSet<>(Arrays.asList(new CallbackView.FailReason[]{
      CallbackView.FailReason.BLACKLISTED,
      CallbackView.FailReason.PERMISSION_DENIED}
  ));

  /**
   * Origin of call to 'fail': CUSTOMER, SITE, USER_ROLE, OGEL_CREATE, CALLBACK, UNKNOWN
   */
  enum Origin {
    CUSTOMER, SITE, USER_ROLE, OGEL_CREATE, CALLBACK, UNKNOWN;
  }

  @Inject
  public FailService(OgelSubmissionDao submissionDao,
                     @Named("maxMinutesRetryAfterFail") int maxMinutesRetryAfterFail) {
    this.submissionDao = submissionDao;
    this.maxMinutesRetryAfterFail = maxMinutesRetryAfterFail;
  }

  /**
   * Updates OgelSubmission with fail details
   */
  void fail(OgelSubmission sub, CallbackView.FailReason failReason, FailService.Origin origin) {
    doFailUpdate(sub, failReason, origin, null);
  }

  /**
   * Updates OgelSubmission with fail details, appending message to lastFailMes
   */
  void failWithMessage(OgelSubmission sub, CallbackView.FailReason failReason, FailService.Origin origin, String message) {
    doFailUpdate(sub, failReason, origin, message);
  }

  /**
   * Updates OgelSubmission fail reason/message
   * Updates OgelSubmission Status to TERMINATED for repeating fail, or a configured terminal fail
   */
  private void doFailUpdate(OgelSubmission sub, CallbackView.FailReason failReason, Origin origin, String message) {
    String failMessage = createFailMessage(failReason, origin, message);
    LOGGER.error(failMessage);

    if (!sub.hasFail()) {
      sub.setFirstFailDateTime(); // Set first fail
    } else {
      // Check for repeating error
      if (sub.getFirstFailDateTime().isBefore(LocalDateTime.now().minus(maxMinutesRetryAfterFail, MINUTES))) {
        LOGGER.info("Repeating Error - setting status to TERMINATED [" + sub.getRequestId() + "]");
        sub.updateStatusToTerminated();
      }
    }

    sub.setFailReason(failReason);
    sub.setLastFailMessage(failMessage);

    // Check if we have a terminal fail reason
    if (terminalFailReasons.contains(failReason)) {
      LOGGER.info("Terminal Fail - setting status to TERMINATED [" + sub.getRequestId() + "]");
      sub.updateStatusToTerminated();
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

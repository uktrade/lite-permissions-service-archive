package uk.gov.bis.lite.permissions.service;

import static java.time.temporal.ChronoUnit.MINUTES;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.util.Util;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response;

@Singleton
class FailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FailService.class);

  // Search terms used to determine FailReason from error message
  private static String TERM_USER_LACKS_SITE_PRIVILEGES = "USER_LACKS_SITE_PRIVILEGES";
  private static String TERM_LACKS_PRIVILEGES = "USER_LACKS_PRIVILEGES";
  private static String TERM_LICENSE_ALREADY_EXISTS = "There is already a licence for OGEL ref";
  private static String TERM_SITE_ALREADY_REGISTERED = "SITE_ALREADY_REGISTERED";
  private static String TERM_BLACKLISTED = "BLACKLISTED";
  private static String TERM_SOAP_FAULT = "soap:Fault";
  private static String TERM_CUSTOMER_NAME_ALREADY_EXISTS = "Customer name already exists";
  private static String TERM_UNCLASSIFIED_ERROR = "UNCLASSIFIED_ERROR";

  private static final Set<String> searchTerms = new HashSet<>(Arrays.asList(
      new String[]{TERM_USER_LACKS_SITE_PRIVILEGES, TERM_LACKS_PRIVILEGES, TERM_LICENSE_ALREADY_EXISTS,
          TERM_SITE_ALREADY_REGISTERED, TERM_BLACKLISTED, TERM_SOAP_FAULT, TERM_CUSTOMER_NAME_ALREADY_EXISTS, TERM_UNCLASSIFIED_ERROR}
  ));

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

  CallbackView.FailReason getFailReasonFromMessage(String message) {
    CallbackView.FailReason reason = CallbackView.FailReason.UNCLASSIFIED;
    String term = searchTerms.stream().filter(message::contains).findFirst().orElse(CallbackView.FailReason.UNCLASSIFIED.name());
    if (term.equals(TERM_USER_LACKS_SITE_PRIVILEGES) || term.equals(TERM_LACKS_PRIVILEGES)) {
      reason = CallbackView.FailReason.PERMISSION_DENIED;
    } else if (term.equals(TERM_SITE_ALREADY_REGISTERED)) {
      reason = CallbackView.FailReason.SITE_ALREADY_REGISTERED;
    } else if (term.equals(TERM_BLACKLISTED)) {
      reason = CallbackView.FailReason.BLACKLISTED;
    } else if (term.equals(TERM_SOAP_FAULT)) {
      reason = CallbackView.FailReason.ENDPOINT_ERROR;
    }
    return reason;
  }

  void fail(OgelSubmission sub, CallbackView.FailReason failReason, FailService.Origin origin) {
    doFailUpdate(sub, failReason, origin);
  }

  void fail(OgelSubmission sub, Response response, FailService.Origin origin) {
    fail(sub, getResponseStatusAndBody(response), origin);
  }

  void fail(OgelSubmission sub, Exception exception, FailService.Origin origin) {
    fail(sub, Util.getInfo(exception), origin);
  }

  void fail(OgelSubmission sub, String message, Origin origin) {
    if (!sub.hasCompleted()) {
      doFailUpdate(sub, message, origin);
    } else {
      if (!sub.isCalledBack()) {
        LOGGER.error(getSubmissionOriginMessage(sub, message, origin.name()));
      }
    }
  }

  private void doFailUpdate(OgelSubmission sub, String message, Origin origin) {
    String failMessage = getSubmissionOriginMessage(sub, message, origin.name());
    LOGGER.error(failMessage);

    // Set first fail, or check to update status to ERROR
    if (!sub.hasFail()) {
      sub.setFirstFailDateTime();
    } else {
      if (sub.getFirstFailDateTime().isBefore(LocalDateTime.now().minus(maxMinutesRetryAfterFail, MINUTES))) {
        LOGGER.info("Terminal failure setting status to ERROR [" + sub.getRequestId() + "]");
        sub.updateStatusToError();
      }
    }

    // We check error message using list of known errors which indicate that the OgelSubmission should stop
    // processing, and we should initiate the callback for it
    if (hasFailReasonTerm(message)) {
      LOGGER.info("Ending Processing[" + sub.getRequestId() + "] Matched On[" + getFailReasonFromMessage(message) + "][");
      sub.updateStatusToError();
    }

    sub.setLastFailMessage(failMessage);
    submissionDao.update(sub);
  }

  private void doFailUpdate(OgelSubmission sub, CallbackView.FailReason failReason, Origin origin) {
    String failMessage = "FailReason[" + failReason.name() + "] Origin[" + origin.name() + "]";
    LOGGER.error(failMessage);

    sub.setFirstFailDateTime();
    sub.updateStatusToError();
    sub.setFailReason(failReason);
    sub.setLastFailMessage(failMessage);
    submissionDao.update(sub);
  }

  private boolean hasFailReasonTerm(String message) {
    return searchTerms.stream().anyMatch(message::contains);
  }

  private static String getResponseStatusAndBody(Response response) {
    String status = "-";
    String body = "-";
    if (response != null) {
      status = "" + response.getStatus();
      if (response.hasEntity()) {
        body = response.readEntity(String.class);
      }
    }
    return "RESPONSE FAIL: " + "Status[" + status + "] Body[" + body + "]";
  }

  private String getSubmissionOriginMessage(OgelSubmission sub, String message, String origin) {
    return "Ogel Submission FAIL [" + sub.getRequestId() + "]" + "[" + origin + "][" + message + "]";
  }

}

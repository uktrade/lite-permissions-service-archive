package uk.gov.bis.lite.permissions.service;

import static java.time.temporal.ChronoUnit.MINUTES;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  /**
   * Enumeration of Callback fail reasons
   */
  private enum FailReason {

    PERMISSION_DENIED, SITE_ALREADY_REGISTERED, BLACKLISTED, ENDPOINT_ERROR, UNCLASSIFIED_ERROR;

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

    public static String getReasonFromMessage(String message) {
      String reason = UNCLASSIFIED_ERROR.name();
      String term = searchTerms.stream().filter(message::contains).findFirst().orElse(UNCLASSIFIED_ERROR.name());
      if (term.equals(TERM_USER_LACKS_SITE_PRIVILEGES) || term.equals(TERM_LACKS_PRIVILEGES)) {
        reason = PERMISSION_DENIED.name();
      } else if (term.equals(TERM_SITE_ALREADY_REGISTERED)) {
        reason = SITE_ALREADY_REGISTERED.name();
      } else if (term.equals(TERM_BLACKLISTED)) {
        reason = BLACKLISTED.name();
      } else if (term.equals(TERM_SOAP_FAULT)) {
        reason = ENDPOINT_ERROR.name();
      }
      return reason;
    }

    public static boolean hasFailTerm(String message) {
      return searchTerms.stream().anyMatch(message::contains);
    }
  }

  void fail(OgelSubmission sub, String info, FailService.Origin origin) {
    fail(sub.getSubmissionRef(), info, origin);
  }

  void fail(OgelSubmission sub, Response response, FailService.Origin origin) {
    fail(sub.getSubmissionRef(), getResponseStatusAndBody(response), origin);
  }

  void fail(OgelSubmission sub, Exception exception, FailService.Origin origin) {
    fail(sub.getSubmissionRef(), Util.getInfo(exception), origin);
  }

  private void fail(String submissionRef, String message, Origin origin) {
    OgelSubmission sub = submissionDao.findBySubmissionRef(submissionRef);
    if (!sub.hasCompleted()) {
      doFailUpdate(sub, message, origin);
    } else {
      // When fail received for 'completed' submission, but before successful callback we log detail of fail
      if (!sub.isCalledBack()) {
        LOGGER.error(getSubmissionOriginMessage(submissionRef, message, origin.name()));
      }
    }
  }

  private void doFailUpdate(OgelSubmission sub, String message, Origin origin) {
    String failMessage = getSubmissionOriginMessage(sub.getSubmissionRef(), message, origin.name());
    LOGGER.error(failMessage);

    // Set first fail, or check to update status to ERROR
    if (!sub.hasFail()) {
      sub.setFirstFailDateTime();
    } else {
      if (sub.getFirstFailDateTime().isBefore(LocalDateTime.now().minus(maxMinutesRetryAfterFail, MINUTES))) {
        LOGGER.info("Terminal failure setting status to ERROR [" + sub.getSubmissionRef() + "]");
        sub.updateStatusToError();
      }
    }

    // We check error message using list of known errors which indicate that the OgelSubmission should stop
    // processing, and we should initiate the callback for it
    if (FailReason.hasFailTerm(message)) {
      LOGGER.info("Ending Processing[" + sub.getSubmissionRef() + "] Matched On[" + getMatchedErrorFromMessage(message) + "][");
      sub.updateStatusToError();
    }

    sub.setLastFailMessage(failMessage);
    submissionDao.update(sub);
  }

  static String getMatchedErrorFromMessage(String message) {
    return FailReason.getReasonFromMessage(message);
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

  private String getSubmissionOriginMessage(String submissionRef, String message, String origin) {
    return "Ogel Submission FAIL [" + submissionRef + "]" + "[" + origin + "][" + message + "]";
  }

}

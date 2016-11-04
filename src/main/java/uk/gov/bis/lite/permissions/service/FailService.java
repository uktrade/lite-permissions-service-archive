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

  private static String USER_LACKS_SITE_PRIVILEGES = "USER_LACKS_SITE_PRIVILEGES";
  private static String USER_LACKS_PRIVILEGES = "USER_LACKS_PRIVILEGES";
  private static String LICENSE_ALREADY_EXISTS_VALUE = "There is already a licence for OGEL ref";
  private static String LICENSE_ALREADY_EXISTS = "LICENSE_ALREADY_EXISTS";
  private static String SITE_ALREADY_REGISTERED = "SITE_ALREADY_REGISTERED";
  private static String BLACKLISTED = "BLACKLISTED";
  private static String SOAP_FAULT_VALUE = "soap:Fault";
  private static String SOAP_FAULT = "SOAP_FAULT";
  private static String CUSTOMER_NAME_ALREADY_EXISTS_VALUE = "Customer name already exists";
  private static String CUSTOMER_NAME_ALREADY_EXISTS = "CUSTOMER_NAME_ALREADY_EXISTS";

  private static final Set<String> endProcessingMessages = new HashSet<>(Arrays.asList(
      new String[]{USER_LACKS_SITE_PRIVILEGES, USER_LACKS_PRIVILEGES, LICENSE_ALREADY_EXISTS_VALUE,
          SITE_ALREADY_REGISTERED, BLACKLISTED, SOAP_FAULT_VALUE, CUSTOMER_NAME_ALREADY_EXISTS_VALUE}
  ));

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

  void fail(OgelSubmission sub, String info, FailService.Origin origin) {
    fail(sub.getSubmissionRef(), info, origin);
  }

  void fail(OgelSubmission sub, Response response, FailService.Origin origin) {
    fail(sub.getSubmissionRef(), getResponseStatusAndBody(response), origin);
  }

  void fail(OgelSubmission sub, Exception exception, FailService.Origin origin) {
    fail(sub.getSubmissionRef(), Util.getInfo(exception), origin);
  }

  void fail(String submissionRef, String message, Origin origin) {
    OgelSubmission sub = submissionDao.findBySubmissionRef(submissionRef);
    if (!sub.hasCompleted()) {
      doFailUpdate(sub, message, origin);
    }
  }

  private void doFailUpdate(OgelSubmission sub, String message, Origin origin) {
    String originMessage = "[" + origin.name() + "][" + message + "]";
    LOGGER.error("Ogel Submission process failure [" + sub.getSubmissionRef() + "]" + originMessage);

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
    boolean endProcessing = endProcessingMessages.stream().anyMatch(message::contains);
    if (endProcessing) {
      LOGGER.info("Ending Processing[" + sub.getSubmissionRef() + "] Matched On[" + getMatchedErrorFromMessage(message) + "][");
      sub.updateStatusToError();
    }

    sub.setLastFailMessage(originMessage);
    submissionDao.update(sub);
  }

  public static String getMatchedErrorFromMessage(String message) {
    String matched = endProcessingMessages.stream().filter(message::contains).findFirst().orElse("UNKNOWN: " + message);
    if(matched.equals(LICENSE_ALREADY_EXISTS_VALUE)) {
      matched = LICENSE_ALREADY_EXISTS;
    }
    if(matched.equals(SOAP_FAULT_VALUE)) {
      matched = SOAP_FAULT;
    }
    if(matched.equals(CUSTOMER_NAME_ALREADY_EXISTS_VALUE)) {
      matched = CUSTOMER_NAME_ALREADY_EXISTS;
    }
    return matched;
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


}

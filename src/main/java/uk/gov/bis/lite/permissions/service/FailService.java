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

  void fail(OgelSubmission sub, String info, FailService.Origin origin) {
    fail(sub.getSubmissionRef(), info, origin);
  }

  void fail(OgelSubmission sub, Response response, FailService.Origin origin) {
    fail(sub.getSubmissionRef(), getResponseInfo(response), origin);
  }

  void fail(OgelSubmission sub, Exception exception, FailService.Origin origin) {
    fail(sub.getSubmissionRef(), Util.getInfo(exception), origin);
  }

  void fail(String submissionRef, String message, Origin origin) {
    OgelSubmission sub = submissionDao.findBySubmissionRef(submissionRef);
    if(!sub.hasCompleted()) {
      doFailUpdate(sub, message, origin);
    }
  }

  private void doFailUpdate(OgelSubmission sub, String message, Origin origin) {
    String originMessage = "[" + origin.name() + "][" + message + "]";
    LOGGER.error("Ogel Submission process failure [" + sub.getSubmissionRef() + "]" + originMessage);

    // Set first fail, or check to update status to ERROR
    if(!sub.hasFail()) {
      sub.setFirstFailDateTime();
    } else {
      if(sub.getFirstFailDateTime().isBefore(LocalDateTime.now().minus(maxMinutesRetryAfterFail, MINUTES))) {
        LOGGER.info("Terminal failure setting status to ERROR [" + sub.getSubmissionRef() + "]");
        sub.updateStatusToError();
      }
    }

    sub.setLastFailMessage(originMessage);
    submissionDao.update(sub);
  }

  private static String getResponseInfo(Response response) {
    String info = "Response is null";
    if(response != null) {
      info = "Status [" + response.getStatus() + " |" + response.readEntity(String.class) + "]";
    }
    return info;
  }
}

package uk.gov.bis.lite.permissions.service;

import static java.time.temporal.ChronoUnit.MINUTES;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.time.LocalDateTime;

@Singleton
public class FailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FailService.class);

  private OgelSubmissionDao submissionDao;
  private int maxMinutesRetryAfterFail;

  /**
   * CUSTOMER     -
   * SITE         -
   * USER_ROLE    -
   * OGEL_CREATE  -
   */
  public enum Origin {
    CUSTOMER, SITE, USER_ROLE, OGEL_CREATE,
  }

  @Inject
  public FailService(OgelSubmissionDao submissionDao,
                     @Named("maxMinutesRetryAfterFail") int maxMinutesRetryAfterFail) {
    this.submissionDao = submissionDao;
    this.maxMinutesRetryAfterFail = maxMinutesRetryAfterFail;
  }

  public void fail(String submissionRef, String message, Origin origin) {
    OgelSubmission sub = submissionDao.findBySubmissionRef(submissionRef);
    if(!sub.hasCompleted()) {
      doFailUpdate(sub, message, origin);
    }
  }

  private void doFailUpdate(OgelSubmission sub, String message, Origin origin) {
    String originMessage = "[" + origin.name() + "][" + message + "]";
    LOGGER.warn("Submission process failure [" + sub.getSubmissionRef() + "]" + originMessage);

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
}

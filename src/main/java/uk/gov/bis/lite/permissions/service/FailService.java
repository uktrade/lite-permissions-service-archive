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
  private int windowMinutes;

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
  public FailService(OgelSubmissionDao submissionDao, @Named("maxFailRetryWindowMinutes") int windowMinutes) {
    this.submissionDao = submissionDao;
    this.windowMinutes = windowMinutes;
  }

  public void fail(String subRef, String message, Origin origin) {
    OgelSubmission sub = submissionDao.findBySubmissionRef(subRef);
    if(!sub.hasCompleted()) {
      doFailUpdate(sub, message, origin);
    }
  }

  private void doFailUpdate(OgelSubmission sub, String message, Origin origin) {
    String originMessage = "[" + origin.name() + "] - " + message;
    LOGGER.warn("OgelSubmission [" + sub.getSubmissionRef() + "]" + originMessage);
    // If subsequent 'fail' and more than 'windowMinutes' in the past, we stop submission, set Status to ERROR
    if(sub.hasFail()) {
      LOGGER.info("hasFail");
      if(sub.getFirstFailDateTime().isBefore(LocalDateTime.now().minus(windowMinutes, MINUTES))) {
        LOGGER.info("updateStatusToError");
        sub.updateStatusToError();
      }
    } else {
      sub.setFirstFailDateTime();
    }
    sub.setLastFailMessage(originMessage);
    submissionDao.update(sub);
  }
}

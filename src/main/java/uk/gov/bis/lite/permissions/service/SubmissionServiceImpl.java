package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.Optional;

@Singleton
public class SubmissionServiceImpl implements SubmissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionServiceImpl.class);

  private OgelSubmissionDao submissionDao;
  private CustomerService customerService;

  private static final String USER_ROLE_UPDATE_STATUS_COMPLETE = "COMPLETE";
  private static final String USER_ROLE_UPDATE_STATUS_ERROR = "Error";

  @Inject
  public SubmissionServiceImpl(OgelSubmissionDao submissionDao, CustomerService customerService) {
    this.submissionDao = submissionDao;
    this.customerService = customerService;
  }

  public boolean submissionCurrentlyExists(String subRef) {
    return submissionDao.findRecentBySubmissionRef(subRef) != null;
  }

  public boolean prepareCustomer(OgelSubmission sub) {
    boolean prepared = true;
    if (sub.needsCustomer()) {
      if (!doGetOrCreateCustomer(sub)) {
        prepared = false;
      }
    }
    return prepared;
  }

  public boolean prepareSite(OgelSubmission sub) {
    boolean prepared = true;
    if (sub.needsSite()) {
      if (!doCreateSite(sub)) {
        prepared = false;
      }
    }
    return prepared;
  }

  public boolean prepareRoleUpdate(OgelSubmission sub) {
    boolean prepared = true;
    if (sub.isRoleUpdate() && !sub.isRoleUpdated()) {
      if (!doUserRoleUpdate(sub)) {
        prepared = false;
      }
    }
    return prepared;
  }

  /**
   * If OgelSubmission has not completed processing or has not yet been 'called back'
   * then set MODE to 'SCHEDULED'
   */
  public void updateModeIfNotCompleted(String submissionRef) {
    OgelSubmission sub = submissionDao.findBySubmissionRef(submissionRef);
    if (!sub.hasCompleted() || !sub.isCalledBack()) {
      LOGGER.info("Updating MODE to SCHEDULED for: [" + submissionRef + "]");
      sub.changeToScheduledMode();
      sub.updateStatus();
      submissionDao.update(sub);
    }
  }

  private boolean doGetOrCreateCustomer(OgelSubmission sub) {
    Optional<String> sarRef = customerService.getOrCreateCustomer(sub);
    boolean created = sarRef.isPresent();
    if (created) {
      sub.setCustomerRef(sarRef.get());
      sub.updateStatus();
      submissionDao.update(sub);
      LOGGER.info("Updated record with created Customer sarRef: " + sarRef.get());
    }
    return created;
  }

  private boolean doCreateSite(OgelSubmission sub) {
    Optional<String> siteRef = customerService.createSite(sub);
    boolean created = siteRef.isPresent();
    if (created) {
      sub.setSiteRef(siteRef.get());
      sub.updateStatus();
      submissionDao.update(sub);
      LOGGER.info("Site created. Updated record: " + siteRef.get());
    }
    return created;
  }

  private boolean doUserRoleUpdate(OgelSubmission sub) {
    Optional<String> status = customerService.updateUserRole(sub);
    boolean created = status.isPresent() && status.get().equals(USER_ROLE_UPDATE_STATUS_COMPLETE);
    if (created) {
      sub.setRoleUpdated(true);
      sub.updateStatus();
      submissionDao.update(sub);
      LOGGER.info("User role updated. Updated OgelSubmission: " + sub.getUserId() + "/" + sub.getOgelType());
    }
    return created;
  }

}

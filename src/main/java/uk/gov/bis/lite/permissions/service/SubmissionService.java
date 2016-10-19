package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.List;
import java.util.Optional;

@Singleton
public class SubmissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionService.class);

  private OgelSubmissionDao submissionDao;
  private CustomerService customerService;

  private static final String USER_ROLE_UPDATE_STATUS_COMPLETE = "COMPLETE";
  private static final String USER_ROLE_UPDATE_STATUS_ERROR = "Error";

  @Inject
  public SubmissionService(OgelSubmissionDao submissionDao, CustomerService customerService) {
    this.submissionDao = submissionDao;
    this.customerService = customerService;
  }

  public boolean submissionCurrentlyExists(String subRef) {
    return submissionDao.findRecentBySubmissionRef(subRef) != null;
  }

  /**
   * If OgelSubmission has not completed processing, set submission mode to SCHEDULED
   */
  public void checkToResetMode(String subRef) {
    LOGGER.info("checkToResetMode [" + subRef + "]");
    OgelSubmission sub = submissionDao.findBySubmissionRef(subRef);
    if (!sub.hasCompleted()) {
      sub.changeToScheduledMode();
      sub.updateStatus();
      submissionDao.update(sub);
    }
  }

  public boolean immediatePrepare(String subRef) {
    LOGGER.info("immediatePrepare [" + subRef + "]");
    boolean allCreated = true;
    OgelSubmission sub = submissionDao.findBySubmissionRef(subRef);
    if (sub != null && sub.isImmediate()) {
      // Create Customer if needed
      if (sub.needsCustomer()) {
        if (!doCreateCustomer(sub)) {
          allCreated = false;
        }
      }
      // Create Site if needed
      if (sub.needsSite() && allCreated) {
        if (!doCreateSite(sub)) {
          allCreated = false;
        }
      }
      // Update User Role if needed
      if (sub.isRoleUpdate() && allCreated) {
        if (!doUserRoleUpdate(sub)) {
          allCreated = false;
        }
      }
    } else {
      LOGGER.warn("Unexpected OgelSubmission state");
    }
    return allCreated;
  }

  public void processScheduled(OgelSubmission.Status status) {
    if (status.equals(OgelSubmission.Status.CUSTOMER)) {
      doScheduledCreateCustomers();
    } else if (status.equals(OgelSubmission.Status.SITE)) {
      doScheduledCreateSites();
    } else if (status.equals(OgelSubmission.Status.USER_ROLE)) {
      doScheduledUserRoleUpdates();
    }
  }

  /**
   * For each OgelSubmission we need to create a Customer on Spire
   * and then update the OgelSubmission status
   */
  private void doScheduledCreateCustomers() {
    List<OgelSubmission> subs = submissionDao.getScheduledByStatus(OgelSubmission.Status.CUSTOMER.name());
    LOGGER.info("CUSTOMERS [" + subs.size() + "]");
    subs.forEach(this::doCreateCustomer);
  }

  private boolean doCreateCustomer(OgelSubmission sub) {
    Optional<String> sarRef = customerService.createCustomer(sub);
    boolean created = sarRef.isPresent();
    if (created) {
      sub.setCustomerRef(sarRef.get());
      sub.updateStatus();
      submissionDao.update(sub);
      LOGGER.info("Customer created. Updated record: " + sarRef.get());
    }
    return created;
  }

  /**
   * For each OgelSubmission we need to create a Site on Spire
   * and then update the OgelSubmission status
   */
  private void doScheduledCreateSites() {
    List<OgelSubmission> subs = submissionDao.getScheduledByStatus(OgelSubmission.Status.SITE.name());
    LOGGER.info("SITES [" + subs.size() + "]");
    subs.forEach(this::doCreateSite);
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

  /**
   * For each OgelSubmission we update site access permissions and
   * then set the OgelSubmission status to READY
   */
  private void doScheduledUserRoleUpdates() {
    List<OgelSubmission> subs = submissionDao.getScheduledByStatus(OgelSubmission.Status.USER_ROLE.name());
    LOGGER.info("USER_ROLES [" + subs.size() + "]");
    subs.forEach(this::doUserRoleUpdate);
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

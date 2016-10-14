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

  public void processOgelSubmissions(OgelSubmission.Status status) {

    if(status.equals(OgelSubmission.Status.CUSTOMER)) {
      doCreateCustomers();
    }
    if(status.equals(OgelSubmission.Status.SITE)) {
      doCreateSites();
    }
    if(status.equals(OgelSubmission.Status.USER_ROLE)) {
      doUserRoleUpdates();
    }
  }

  /**
   * For each OgelSubmission we need to create a Customer on Spire
   * and then update the OgelSubmission status
   */
  private void doCreateCustomers() {
    List<OgelSubmission> subs = submissionDao.getByStatus(OgelSubmission.Status.CUSTOMER.name());
    LOGGER.info("Found CUSTOMER [" + subs.size() + "]");
    for(OgelSubmission sub : subs) {
      Optional<String> sarRef = customerService.createCustomer(sub);
      if(sarRef.isPresent()) {
        sub.setCustomerRef(sarRef.get());
        sub.updateStatus();
        submissionDao.update(sub);
        LOGGER.info("Customer created. Updated record: " + sarRef.get());
      }
    }
  }

  /**
   * For each OgelSubmission we need to create a Site on Spire
   * and then update the OgelSubmission status
   */
  private void doCreateSites() {
    List<OgelSubmission> subs = submissionDao.getByStatus(OgelSubmission.Status.SITE.name());
    LOGGER.info("Found SITE [" + subs.size() + "]");
    for(OgelSubmission sub : subs) {
      Optional<String> siteRef = customerService.createSite(sub);
      if(siteRef.isPresent()) {
        sub.setSiteRef(siteRef.get());
        sub.updateStatus();
        submissionDao.update(sub);
        LOGGER.info("Site created. Updated record: " + siteRef.get());
      }
    }
  }

  /**
   * For each OgelSubmission we update site access permissions and
   * then set the OgelSubmission status to READY
   */
  private void doUserRoleUpdates() {
    List<OgelSubmission> subs = submissionDao.getByStatus(OgelSubmission.Status.USER_ROLE.name());
    LOGGER.info("Found USER_ROLE [" + subs.size() + "]");
    for(OgelSubmission sub : subs) {
      Optional<String> status = customerService.updateUserRole(sub);
      if(status.isPresent() && status.get().equals(USER_ROLE_UPDATE_STATUS_COMPLETE)) {
        sub.updateStatusToReady();
        submissionDao.update(sub);
        LOGGER.info("User role updated. Updated OgelSubmission to READY: " + sub.getUserId() + "/" + sub.getOgelType());
      }
    }
  }

}

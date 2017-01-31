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

  @Inject
  public SubmissionServiceImpl(OgelSubmissionDao submissionDao, CustomerService customerService) {
    this.submissionDao = submissionDao;
    this.customerService = customerService;
  }

  public boolean submissionCurrentlyExists(String subRef) {
    return submissionDao.findRecentBySubmissionRef(subRef) != null;
  }

  /**
   * Returns TRUE if CUSTOMER stage has successfully completed
   */
  public boolean processForCustomer(OgelSubmission sub) {
    boolean processed = false;
    if (doGetOrCreateCustomer(sub)) {
      processed = true;
    }
    return processed;
  }

  /**
   * Returns TRUE if SITE stage has successfully completed
   */
  public boolean processForSite(OgelSubmission sub) {
    boolean processed = false;
    if (doCreateSite(sub)) {
      processed = true;
    }
    return processed;
  }

  /**
   * Returns TRUE if USER_ROLE stage has successfully completed
   */
  public boolean processForRoleUpdate(OgelSubmission sub) {
    boolean processed = false;
    if (doUserRoleUpdate(sub)) {
      processed = true;
    }
    return processed;
  }

  private boolean doGetOrCreateCustomer(OgelSubmission sub) {
    Optional<String> sarRef = customerService.getOrCreateCustomer(sub);
    boolean created = sarRef.isPresent();
    if (created) {
      sub.setCustomerRef(sarRef.get());
      LOGGER.info("[" + sub.getId() + "] OgelSubmission CUSTOMER created: " + sarRef.get());
    }
    return created;
  }

  private boolean doCreateSite(OgelSubmission sub) {
    Optional<String> siteRef = customerService.createSite(sub);
    boolean created = siteRef.isPresent();
    if (created) {
      sub.setSiteRef(siteRef.get());
      LOGGER.info("[" + sub.getId() + "] OgelSubmission SITE created: " + siteRef.get());
    }
    return created;
  }

  private boolean doUserRoleUpdate(OgelSubmission sub) {
    boolean updated = customerService.updateUserRole(sub);
    if (updated) {
      sub.setRoleUpdated(true);
      LOGGER.info("[" + sub.getId() + "] OgelSubmission USER_ROLE updated: " + sub.getUserId() + "/" + sub.getOgelType());
    }
    return updated;
  }

}

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

  public boolean processForCustomer(OgelSubmission sub) {
    boolean processed = true;
    if (!sub.hasCompletedStage(OgelSubmission.Stage.CUSTOMER)) {
      if (!doGetOrCreateCustomer(sub)) {
        processed = false;
      }
    }
    return processed;
  }

  public boolean processForSite(OgelSubmission sub) {
    boolean processed = true;
    if (!sub.hasCompletedStage(OgelSubmission.Stage.SITE)) {
      if (!doCreateSite(sub)) {
        processed = false;
      }
    }
    return processed;
  }

  public boolean processForRoleUpdate(OgelSubmission sub) {
    boolean processed = true;
    if (!sub.hasCompletedStage(OgelSubmission.Stage.USER_ROLE)) {
      if (!doUserRoleUpdate(sub)) {
        processed = false;
      }
    }
    return processed;
  }

  private boolean doGetOrCreateCustomer(OgelSubmission sub) {
    Optional<String> sarRef = customerService.getOrCreateCustomer(sub);
    boolean created = sarRef.isPresent();
    if (created) {
      sub.setCustomerRef(sarRef.get());
      sub.updateToNextStage();
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
      sub.updateToNextStage();
      submissionDao.update(sub);
      LOGGER.info("Site created. Updated record: " + siteRef.get());
    }
    return created;
  }

  private boolean doUserRoleUpdate(OgelSubmission sub) {
    boolean updated = customerService.updateUserRole(sub);
    if (updated) {
      sub.setRoleUpdated(true);
      sub.updateToNextStage();
      submissionDao.update(sub);
      LOGGER.info("User role updated. Updated OgelSubmission: " + sub.getUserId() + "/" + sub.getOgelType());
    }
    return updated;
  }

}

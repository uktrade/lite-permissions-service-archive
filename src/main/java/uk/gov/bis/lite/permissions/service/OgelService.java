package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.spire.SpireRefResponse;
import uk.gov.bis.lite.permissions.spire.SpireService;
import uk.gov.bis.lite.permissions.spire.model.OgelAppItem;
import uk.gov.bis.lite.permissions.util.Util;

import javax.ws.rs.core.Response;

@Singleton
public class OgelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelService.class);

  private SpireService spireService;
  private FailService failService;
  private OgelSubmissionDao submissionDao;

  @Inject
  public OgelService(SpireService spireService, FailService failService, OgelSubmissionDao submissionDao) {
    this.spireService = spireService;
    this.failService = failService;
    this.submissionDao = submissionDao;
  }

  boolean createOgel(String submissionRef) {
    LOGGER.info("createOgel [" + submissionRef + "]");
    boolean created = false;
    OgelSubmission sub = submissionDao.findBySubmissionRef(submissionRef);
    if (sub != null) {
      if(sub.canCreateOgel()) {
        created = doCreateOgel(sub);
      } else {
        LOGGER.warn("Cannot create Ogel - OgelSubmission state is not complete");
      }
    }
    return created;
  }

  private boolean doCreateOgel(OgelSubmission sub) {
    OgelAppItem item = getOgelAppItem(sub);
    SpireRefResponse spireRefResponse = spireService.createOgelApp(item);
    boolean created = spireRefResponse.hasRef();
    if (created) {
      String spireRef = spireRefResponse.getRef();
      sub.setSpireRef(spireRef);
      sub.updateStatusToSuccess();
      submissionDao.update(sub);
    } else {
      LOGGER.warn("Create Ogel Error [" + sub.getSubmissionRef() + "]");
      failService.fail(sub.getSubmissionRef(), spireRefResponse.getErrorMessage(), FailService.Origin.OGEL_CREATE);
    }
    return created;
  }

  private OgelAppItem getOgelAppItem(OgelSubmission sub) {
    OgelAppItem item = new OgelAppItem();
    item.setUserId(sub.getUserId());
    item.setSarRef(sub.getCustomerRef());
    item.setSiteRef(sub.getSiteRef());
    item.setOgelType(sub.getOgelType());
    return item;
  }

  private void notifyFail(OgelSubmission sub, Response response, FailService.Origin origin) {
    failService.fail(sub.getSubmissionRef(), Util.getInfo(response), origin);
  }
}

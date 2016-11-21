package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.spire.client.SpireRequest;
import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.spire.SpireReferenceClient;
import uk.gov.bis.lite.permissions.util.Util;

@Singleton
public class OgelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelService.class);

  private SpireReferenceClient createOgelAppReferenceClient;
  private FailService failService;
  private OgelSubmissionDao submissionDao;

  @Inject
  public OgelService(SpireReferenceClient createOgelAppReferenceClient, FailService failService, OgelSubmissionDao submissionDao) {
    this.createOgelAppReferenceClient = createOgelAppReferenceClient;
    this.failService = failService;
    this.submissionDao = submissionDao;
  }

  boolean createOgel(OgelSubmission sub) {
    boolean created = false;
    if (!StringUtils.isBlank(sub.getSpireRef())) {
      created = true; // we check whether we already have created this Ogel
    } else if (sub.canCreateOgel()) {
      created = doCreateOgel(sub);
    } else {
      LOGGER.warn("Cannot create Ogel - OgelSubmission state is not complete");
    }
    return created;
  }

  private boolean doCreateOgel(OgelSubmission sub) {
    SpireRequest request = createOgelAppReferenceClient.createRequest();
    request.addChild("VERSION_NO", "1.0");
    request.addChild("WUA_ID", sub.getUserId());
    request.addChild("SAR_REF", sub.getCustomerRef());
    request.addChild("SITE_REF", sub.getSiteRef());
    request.addChildList("OGL_TYPE_LIST", "OGL_TYPE", "TYPE", sub.getOgelType());

    // Execute Spire Request
    boolean created = false;
    try {
      String reference = createOgelAppReferenceClient.sendRequest(request);
      if (!StringUtils.isBlank(reference)) {
        created = true;
        sub.setSpireRef(reference);
        sub.updateStatusToSuccess();
        submissionDao.update(sub);
        LOGGER.info("STATUS: " + sub.getStatus().name());
      } else {
        failService.failWithMessage(sub, CallbackView.FailReason.UNCLASSIFIED, FailService.Origin.OGEL_CREATE, "No Spire reference returned");
      }
    } catch (SpireClientException e) {
      String info = Util.info(e);
      if (info.contains(FailService.BLACKLISTED)) {
        failService.fail(sub, CallbackView.FailReason.BLACKLISTED, FailService.Origin.OGEL_CREATE);
      } else if (info.contains(FailService.USER_LACKS_SITE_PRIVILEGES) || info.contains(FailService.USER_LACKS_PRIVILEGES)) {
        failService.fail(sub, CallbackView.FailReason.PERMISSION_DENIED, FailService.Origin.OGEL_CREATE);
      } else {
        failService.failWithMessage(sub, CallbackView.FailReason.UNCLASSIFIED, FailService.Origin.OGEL_CREATE, info);
      }
    }
    return created;
  }

}

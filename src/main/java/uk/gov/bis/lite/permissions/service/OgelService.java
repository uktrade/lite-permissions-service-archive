package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.spire.SpireClient;
import uk.gov.bis.lite.spire.SpireName;
import uk.gov.bis.lite.spire.SpireRequest;
import uk.gov.bis.lite.spire.SpireResponse;
import uk.gov.bis.lite.spire.SpireUnmarshaller;
import uk.gov.bis.lite.spire.exception.SpireException;

@Singleton
public class OgelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelService.class);

  private SpireClient spireClient;
  private SpireUnmarshaller spireUnmarshaller;

  private FailService failService;
  private OgelSubmissionDao submissionDao;

  @Inject
  public OgelService(SpireClient spireClient, SpireUnmarshaller spireUnmarshaller,
                     FailService failService, OgelSubmissionDao submissionDao) {
    this.spireClient = spireClient;
    this.spireUnmarshaller = spireUnmarshaller;
    this.failService = failService;
    this.submissionDao = submissionDao;
  }

  boolean createOgel(OgelSubmission sub) {
    boolean created = false;
    if(!StringUtils.isBlank(sub.getSpireRef())) {
      created = true; // we check whether we already have created this Ogel
    } else if(sub.canCreateOgel()) {
      created = doCreateOgel(sub);
    } else {
      LOGGER.warn("Cannot create Ogel - OgelSubmission state is not complete");
    }
    return created;
  }

  private boolean doCreateOgel(OgelSubmission sub) {

    // Setup SpireRequest
    SpireRequest request = spireClient.createRequest(SpireClient.Endpoint.CREATE_OGEL_APP);
    request.addChild(SpireName.VERSION_NO, SpireName.VERSION_1_0);
    request.addChild(SpireName.WUA_ID, sub.getUserId());
    request.addChild(SpireName.SAR_REF, sub.getCustomerRef());
    request.addChild(SpireName.SITE_REF, sub.getSiteRef());
    request.addChildList(SpireName.OGL_TYPE_LIST, SpireName.OGL_TYPE, SpireName.TYPE, sub.getOgelType());

    // Execute Spire Request
    boolean created = false;
    try {
      // Get SpireResponse and unmarshall
      SpireResponse response = spireClient.sendRequest(request);
      String spireReference =  spireUnmarshaller.getSingleResponseElementContent(response);
      if (!StringUtils.isBlank(spireReference)) {
        created = true;
        sub.setSpireRef(spireReference);
        sub.updateStatusToSuccess();
        submissionDao.update(sub);
        LOGGER.info("STATUS: " + sub.getStatus().name());
      } else {
        failService.fail(sub, "No Spire reference returned", FailService.Origin.OGEL_CREATE);
      }
    } catch (SpireException e) {
      failService.fail(sub, e, FailService.Origin.OGEL_CREATE);
    }
    return created;
  }

}

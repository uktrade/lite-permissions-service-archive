package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.spireclient.SpireClientService;
import uk.gov.bis.lite.spireclient.model.SpireRequest;
import uk.gov.bis.lite.spireclient.model.SpireResponse;
import uk.gov.bis.lite.spireclient.spire.SpireException;

@Singleton
public class OgelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelService.class);

  private SpireClientService spireClient;
  private FailService failService;
  private OgelSubmissionDao submissionDao;

  @Inject
  public OgelService(SpireClientService spireClient, FailService failService, OgelSubmissionDao submissionDao) {
    this.spireClient = spireClient;
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

    // Setup Spire request
    SpireRequest request = spireClient.getSpireRequest(SpireClientService.Endpoint.CREATE_OGEL_APP, sub.getUserId());
    request.setSarRef(sub.getCustomerRef());
    request.setSiteRef(sub.getSiteRef());
    request.setOgelType(sub.getOgelType());

    // Execute Spire Request
    boolean created = false;
    try {
      SpireResponse response = spireClient.executeRequest(request);
      created = response.hasRef();
      if (created) {
        sub.setSpireRef(response.getRef());
        sub.updateStatusToSuccess();
        submissionDao.update(sub);
        LOGGER.info("STATUS: " + sub.getStatus().name());
      } else {
        failService.fail(sub, response, FailService.Origin.OGEL_CREATE);
      }
    } catch (SpireException e) {
      failService.fail(sub, e, FailService.Origin.OGEL_CREATE);
    }

    return created;
  }

}

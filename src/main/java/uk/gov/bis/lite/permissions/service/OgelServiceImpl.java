package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.spire.client.SpireRequest;
import uk.gov.bis.lite.permissions.exception.SpireFailReasonException;
import uk.gov.bis.lite.permissions.model.FailEvent;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.spire.SpireReferenceClient;

import java.util.Optional;

@Singleton
public class OgelServiceImpl implements OgelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelServiceImpl.class);

  private SpireReferenceClient createOgelAppReferenceClient;

  @Inject
  public OgelServiceImpl(SpireReferenceClient createOgelAppReferenceClient) {
    this.createOgelAppReferenceClient = createOgelAppReferenceClient;
  }

  public Optional<String> createOgel(OgelSubmission sub) {
    SpireRequest request = createOgelAppReferenceClient.createRequest();
    request.addChild("VERSION_NO", "1.0");
    request.addChild("WUA_ID", sub.getUserId());
    request.addChild("SAR_REF", sub.getCustomerRef());
    request.addChild("SITE_REF", sub.getSiteRef());
    request.addChildList("OGL_TYPE_LIST", "OGL_TYPE", "TYPE", sub.getOgelType());

    // Execute Spire Request
    try {
      String reference = createOgelAppReferenceClient.sendRequest(request);
      if (!StringUtils.isBlank(reference)) {
        return Optional.of(reference);
      } else {
        sub.setFailEvent(new FailEvent(OgelSubmission.FailReason.UNCLASSIFIED, ProcessSubmissionServiceImpl.Origin.OGEL_CREATE, "No Spire reference returned"));
      }
    } catch (SpireFailReasonException e) {
      sub.setFailEvent(new FailEvent(e.getFailReason(), ProcessSubmissionServiceImpl.Origin.OGEL_CREATE, e.getMessage()));
    } catch (Throwable e) {
      sub.setFailEvent(new FailEvent(OgelSubmission.FailReason.UNCLASSIFIED, ProcessSubmissionServiceImpl.Origin.OGEL_CREATE, e.getMessage()));
    }

    return Optional.empty();
  }

}

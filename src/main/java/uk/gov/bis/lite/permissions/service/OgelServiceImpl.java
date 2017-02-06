package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.spire.client.SpireRequest;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.exception.SpireFailReasonException;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.spire.SpireReferenceClient;

import java.util.Optional;

@Singleton
public class OgelServiceImpl implements OgelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelServiceImpl.class);

  private SpireReferenceClient createOgelAppReferenceClient;
  private FailService failService;

  @Inject
  public OgelServiceImpl(SpireReferenceClient createOgelAppReferenceClient, FailService failService) {
    this.createOgelAppReferenceClient = createOgelAppReferenceClient;
    this.failService = failService;
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
        failService.failWithMessage(sub, CallbackView.FailReason.UNCLASSIFIED, FailServiceImpl.Origin.OGEL_CREATE, "No Spire reference returned");
      }
    } catch (SpireFailReasonException e) {
      failService.failWithMessage(sub, e.getFailReason(), FailServiceImpl.Origin.OGEL_CREATE, e.getMessage());
    }
    return Optional.empty();
  }

}

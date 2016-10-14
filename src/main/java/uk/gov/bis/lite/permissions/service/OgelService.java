package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.spireclient.ClientCreateOgelApp;
import uk.gov.bis.lite.permissions.spireclient.ClientUnmarshaller;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.List;
import java.util.Optional;

import javax.xml.soap.SOAPMessage;

@Singleton
public class OgelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelService.class);

  private ClientCreateOgelApp clientCreateOgelApp;
  private OgelSubmissionDao submissionDao;
  private ClientUnmarshaller clientUnmarshaller;

  private static final String CLS_RESPONSE_ELEMENT_NAME = "SPIRE_REF";
  private static final String CLS_SAR_XPATH_EXPRESSION = "//*[local-name()='RESPONSE']";

  @Inject
  public OgelService(ClientCreateOgelApp clientCreateOgelApp, ClientUnmarshaller clientUnmarshaller, OgelSubmissionDao submissionDao) {
    this.clientCreateOgelApp = clientCreateOgelApp;
    this.submissionDao = submissionDao;
    this.clientUnmarshaller = clientUnmarshaller;
  }

  public void doCreateOgels() {
    List<OgelSubmission> subs = submissionDao.getByStatus(OgelSubmission.Status.READY.name());
    LOGGER.info("Found READY [" + subs.size() + "]");
    for(OgelSubmission sub : subs) {

      SOAPMessage message = clientCreateOgelApp.createOgelApp(
          sub.getUserId(),
          sub.getCustomerRef(),
          sub.getSiteRef(),
          sub.getOgelType());

      Optional<String> result = clientUnmarshaller.getResponse(message, CLS_RESPONSE_ELEMENT_NAME, CLS_SAR_XPATH_EXPRESSION);

      if(result.isPresent()) {
        String spireRef = result.get();
        sub.setSpireRef(spireRef);
        sub.updateStatusToComplete();
        submissionDao.update(sub);
      } else {
        LOGGER.warn("Unable to complete Ogel Registration for: " + sub.getSubmissionRef());
      }

    }


  }
}

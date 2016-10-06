package uk.gov.bis.lite.permissions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.client.OgelRegistrationsClient;
import uk.gov.bis.lite.permissions.client.unmarshall.OgelRegistrationsUnmarshaller;
import uk.gov.bis.lite.permissions.dao.OgelRegistrationDao;
import uk.gov.bis.lite.permissions.model.OgelReg;
import uk.gov.bis.lite.permissions.model.OgelRegistration;
import uk.gov.bis.lite.permissions.model.request.RegisterOgel;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.xml.soap.SOAPMessage;

@Singleton
public class RegisterService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterService.class);

  private OgelRegistrationsClient client;
  private OgelRegistrationDao dao;
  private OgelRegistrationsUnmarshaller unmarshaller;

  @Inject
  public RegisterService(OgelRegistrationsClient client,
                         OgelRegistrationsUnmarshaller unmarshaller,
                         OgelRegistrationDao dao) {
    this.client = client;
    this.unmarshaller = unmarshaller;
    this.dao = dao;
  }

  public void register(RegisterOgel regOgel) {
    ObjectMapper mapper = new ObjectMapper();
    OgelRegistration ogel = new OgelRegistration(regOgel.getUserId(), regOgel.getOgelType());
    ogel.setCustomerId(regOgel.getExistingCustomer());
    ogel.setSiteId(regOgel.getExistingSite());
    ogel.setLiteId(regOgel.getIdentifier());
    try {
      ogel.setJson(mapper.writeValueAsString(regOgel));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    dao.create(ogel);
  }

  /**
   * If the OGEL registration request is already in the pending local queue1, respond with PENDING
   * A registration request is considered to already be in the pending local queue if all the field values of
   * the incoming request exactly match the field values of an object in the queue.
   */
  public boolean isPending(RegisterOgel registerOgel) {

    // registerOgel.setResponseMessage("Registration is PENDING");
    return false;
  }

  /**
   * If status is PERMISSION_DENIED (not yet implemented on SPIRE), respond with PERMISSION_DENIED2
   * If status is BLACKLISTED or SITE_ALREADY_REGISTERED, respond with that status
   * Otherwise, assume valid
   */
  public boolean isSpirePermitted(RegisterOgel registerOgel) {

    // registerOgel.setResponseMessage("SITE_ALREADY_REGISTERED");
    return true;
  }

  public void register(String userId, String ogelType) {
    SOAPMessage soapMessage = client.checkOgelStatus(userId);
    List<OgelReg> ogelRegs = unmarshaller.execute(soapMessage);
    LOGGER.info("register ogelRegs: " + ogelRegs.size());
  }

}

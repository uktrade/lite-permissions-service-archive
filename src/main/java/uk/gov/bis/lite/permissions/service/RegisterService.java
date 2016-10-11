package uk.gov.bis.lite.permissions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelRegistrationDao;
import uk.gov.bis.lite.permissions.model.OgelRegistration;
import uk.gov.bis.lite.permissions.model.request.RegisterOgel;

@Singleton
public class RegisterService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterService.class);

  private SoapService soap;
  private OgelRegistrationDao dao;

  @Inject
  public RegisterService(SoapService soap, OgelRegistrationDao dao) {
    this.soap = soap;
    this.dao = dao;
  }

  public void register(RegisterOgel regOgel) {
    LOGGER.info("Registering new Ogel request: " + regOgel.getUserId() + "/" + regOgel.getOgelType());
    OgelRegistration reg = getOgelRegistration(regOgel);
    reg.setInitialStatus();

    // Check if we already have this OgelRegistration request
    OgelRegistration existing = dao.findByLiteId(reg.getLiteId());
    if(existing != null) {
      LOGGER.info("OgelRegistration request already exists, current status: " + reg.getStatus().name());
    } else {
      dao.create(reg);
    }
  }

  /**
   * If the OGEL registration request is already in the pending local queue1, respond with PENDING
   * A registration request is considered to already be in the pending local queue if all the field values of
   * the incoming request exactly match the field values of an object in the queue.
   */
  public boolean isPending(RegisterOgel registerOgel) {
    return false;
  }

  /**
   * If status is PERMISSION_DENIED (not yet implemented on SPIRE), respond with PERMISSION_DENIED2
   * If status is BLACKLISTED or SITE_ALREADY_REGISTERED, respond with that status
   * Otherwise, assume valid
   */
  public boolean isSpirePermitted(RegisterOgel registerOgel) {
    return true;
  }

  private OgelRegistration getOgelRegistration(RegisterOgel regOgel) {
    ObjectMapper mapper = new ObjectMapper();
    OgelRegistration ogel = new OgelRegistration(regOgel.getUserId(), regOgel.getOgelType());
    ogel.setCustomerId(regOgel.getExistingCustomer());
    ogel.setSiteId(regOgel.getExistingSite());
    ogel.setLiteId(regOgel.getHashIdentifier());
    try {
      ogel.setJson(mapper.writeValueAsString(regOgel).replaceAll("\\s+", "")); // remove whitespace
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return ogel;
  }
}

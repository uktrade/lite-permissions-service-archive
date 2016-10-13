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

  public boolean register(RegisterOgel regOgel) {
    LOGGER.info("Registering new Ogel request: " + regOgel.getUserId() + "/" + regOgel.getOgelType());
    OgelRegistration reg = getOgelRegistration(regOgel);
    reg.setInitialStatus();

    // Check if we already have this request
    if (dao.findByLiteId(reg.getLiteId()) != null) {
      LOGGER.info("OgelRegistration request already exists, current status: " + reg.getStatus().name());
      return false;
    }

    dao.create(reg);
    return true;
  }

  private OgelRegistration getOgelRegistration(RegisterOgel reg) {
    ObjectMapper mapper = new ObjectMapper();
    OgelRegistration ogel = new OgelRegistration(reg.getUserId(), reg.getOgelType());
    ogel.setCustomerId(reg.getExistingCustomer());
    ogel.setSiteId(reg.getExistingSite());
    ogel.setLiteId(reg.getHashIdentifier());
    ogel.setRoleUpdate(reg.isRoleUpdateRequired());
    try {
      //ogel.setJson(mapper.writeValueAsString(reg).replaceAll("\\s+", "")); // remove whitespace
      ogel.setJson(mapper.writeValueAsString(reg).replaceAll("\\s{2,}", " ").trim()); // remove excessive whitespace
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return ogel;
  }
}

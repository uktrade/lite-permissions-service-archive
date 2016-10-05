package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.client.OgelRegistrationsClient;
import uk.gov.bis.lite.permissions.client.unmarshall.OgelRegistrationsUnmarshaller;
import uk.gov.bis.lite.permissions.dao.OgelRegistrationDao;
import uk.gov.bis.lite.permissions.model.OgelReg;
import uk.gov.bis.lite.permissions.model.request.RegisterOgel;

import java.util.List;

import javax.xml.soap.SOAPMessage;

@Singleton
public class RegisterService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterService.class);

  private OgelRegistrationsClient client;
  private OgelRegistrationDao dao;
  OgelRegistrationsUnmarshaller unmarshaller;

  @Inject
  public RegisterService(OgelRegistrationsClient client,
                         OgelRegistrationsUnmarshaller unmarshaller,
                         OgelRegistrationDao dao) {
    this.client = client;
    this.unmarshaller = unmarshaller;
    this.dao = dao;
  }

  public boolean isRegistrationPermitted(RegisterOgel registerOgel) {
    return true;
  }

  public void startRegistrationProcess(RegisterOgel registerOgel) {

  }

  public void register(String userId, String ogelType) {
    SOAPMessage soapMessage = client.checkOgelStatus(userId);
    List<OgelReg> ogelRegs = unmarshaller.execute(soapMessage);
    LOGGER.info("register ogelRegs: " + ogelRegs.size());
  }

}

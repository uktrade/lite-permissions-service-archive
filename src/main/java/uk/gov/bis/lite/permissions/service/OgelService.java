package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelRegistrationDao;
import uk.gov.bis.lite.permissions.model.OgelRegistration;

import java.util.List;

@Singleton
public class OgelService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelService.class);

  private SoapService soap;
  private OgelRegistrationDao dao;

  @Inject
  public OgelService(SoapService soap, OgelRegistrationDao dao) {
    this.soap = soap;
    this.dao = dao;
  }

  public void doCreateOgels() {
    List<OgelRegistration> regs = dao.getByStatus(OgelRegistration.Status.READY.name());
    LOGGER.info("Found READY [" + regs.size() + "]");
    for(OgelRegistration reg : regs) {

    }
  }
}

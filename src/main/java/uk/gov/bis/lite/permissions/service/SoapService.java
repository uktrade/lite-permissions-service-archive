package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.client.OgelRegistrationsClient;
import uk.gov.bis.lite.permissions.client.unmarshall.OgelRegistrationsUnmarshaller;
import uk.gov.bis.lite.permissions.dao.OgelRegistrationDao;

@Singleton
public class SoapService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterService.class);

  private OgelRegistrationsClient client;
  private OgelRegistrationsUnmarshaller unmarshaller;

  @Inject
  public SoapService(OgelRegistrationsClient client,
                         OgelRegistrationsUnmarshaller unmarshaller,
                         OgelRegistrationDao dao) {
    this.client = client;
    this.unmarshaller = unmarshaller;
  }
}

package uk.gov.bis.lite.permissions;

import com.google.inject.AbstractModule;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.mocks.OgelSubmissionDaoMock;
import uk.gov.bis.lite.permissions.mocks.pact.RegisterServiceMock;
import uk.gov.bis.lite.permissions.mocks.pact.RegistrationServiceMock;
import uk.gov.bis.lite.permissions.service.RegisterService;
import uk.gov.bis.lite.permissions.service.RegistrationService;

public class PactGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(OgelSubmissionDao.class).to(OgelSubmissionDaoMock.class);
    bind(RegistrationService.class).to(RegistrationServiceMock.class);
    bind(RegisterService.class).to(RegisterServiceMock.class);
  }
}

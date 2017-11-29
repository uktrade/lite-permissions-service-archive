package uk.gov.bis.lite.permissions;

import com.google.inject.AbstractModule;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.mocks.LicenceServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelSubmissionDaoMock;
import uk.gov.bis.lite.permissions.mocks.RegisterServiceMock;
import uk.gov.bis.lite.permissions.mocks.RegistrationServiceMock;
import uk.gov.bis.lite.permissions.service.LicenceService;
import uk.gov.bis.lite.permissions.service.RegisterService;
import uk.gov.bis.lite.permissions.service.RegistrationService;

public class PactGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(OgelSubmissionDao.class).to(OgelSubmissionDaoMock.class);
    bind(RegistrationService.class).to(RegistrationServiceMock.class);
    bind(LicenceService.class).to(LicenceServiceMock.class);
    bind(RegisterService.class).to(RegisterServiceMock.class);
  }
}

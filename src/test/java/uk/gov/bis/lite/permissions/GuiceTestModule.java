package uk.gov.bis.lite.permissions;

import uk.gov.bis.lite.permissions.config.GuiceModule;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDaoImpl;
import uk.gov.bis.lite.permissions.mocks.CustomerServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelServiceMock;
import uk.gov.bis.lite.permissions.service.CallbackService;
import uk.gov.bis.lite.permissions.service.CallbackServiceImpl;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.OgelService;
import uk.gov.bis.lite.permissions.service.ProcessSubmissionService;
import uk.gov.bis.lite.permissions.service.ProcessSubmissionServiceImpl;
import uk.gov.bis.lite.permissions.service.RegisterService;
import uk.gov.bis.lite.permissions.service.RegisterServiceImpl;
import uk.gov.bis.lite.permissions.service.RegistrationsService;
import uk.gov.bis.lite.permissions.service.RegistrationsServiceImpl;
import uk.gov.bis.lite.permissions.service.SubmissionService;
import uk.gov.bis.lite.permissions.service.SubmissionServiceImpl;

/**
 * Use with PermissionTestApp for integration tests - see ProcessSubmissionServiceTest
 */
public class GuiceTestModule extends GuiceModule {

  @Override
  protected void configure() {
    bind(OgelSubmissionDao.class).to(OgelSubmissionDaoImpl.class);
    bind(RegisterService.class).to(RegisterServiceImpl.class);
    bind(RegistrationsService.class).to(RegistrationsServiceImpl.class);
    bind(SubmissionService.class).to(SubmissionServiceImpl.class);
    bind(CallbackService.class).to(CallbackServiceImpl.class);
    bind(ProcessSubmissionService.class).to(ProcessSubmissionServiceImpl.class);

    // Mocked for testing
    bind(CustomerService.class).to(CustomerServiceMock.class);
    bind(OgelService.class).to(OgelServiceMock.class);
  }
}

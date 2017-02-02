package uk.gov.bis.lite.permissions;

import uk.gov.bis.lite.permissions.config.GuiceModule;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDaoImpl;
import uk.gov.bis.lite.permissions.mocks.CustomerServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelServiceMock;
import uk.gov.bis.lite.permissions.service.CallbackService;
import uk.gov.bis.lite.permissions.service.CallbackServiceImpl;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.FailService;
import uk.gov.bis.lite.permissions.service.FailServiceImpl;
import uk.gov.bis.lite.permissions.service.OgelService;
import uk.gov.bis.lite.permissions.service.OgelSubmissionService;
import uk.gov.bis.lite.permissions.service.OgelSubmissionServiceImpl;
import uk.gov.bis.lite.permissions.service.ProcessOgelSubmissionService;
import uk.gov.bis.lite.permissions.service.ProcessOgelSubmissionServiceImpl;
import uk.gov.bis.lite.permissions.service.RegisterService;
import uk.gov.bis.lite.permissions.service.RegisterServiceImpl;
import uk.gov.bis.lite.permissions.service.RegistrationsService;
import uk.gov.bis.lite.permissions.service.RegistrationsServiceImpl;

public class GuiceTestModule extends GuiceModule {

  @Override
  protected void configure() {
    bind(OgelSubmissionDao.class).to(OgelSubmissionDaoImpl.class);
    bind(RegisterService.class).to(RegisterServiceImpl.class);
    bind(RegistrationsService.class).to(RegistrationsServiceImpl.class);
    bind(OgelSubmissionService.class).to(OgelSubmissionServiceImpl.class);
    bind(CallbackService.class).to(CallbackServiceImpl.class);
    bind(ProcessOgelSubmissionService.class).to(ProcessOgelSubmissionServiceImpl.class);
    bind(FailService.class).to(FailServiceImpl.class);

    bind(CustomerService.class).to(CustomerServiceMock.class);
    bind(OgelService.class).to(OgelServiceMock.class);
  }
}

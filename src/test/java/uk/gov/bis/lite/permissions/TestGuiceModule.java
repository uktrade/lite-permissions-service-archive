package uk.gov.bis.lite.permissions;

import com.google.inject.AbstractModule;
import uk.gov.bis.lite.permissions.mocks.CustomerServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelServiceMock;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.OgelService;

/**
 * Use with PermissionTestApp for integration tests - see ProcessSubmissionServiceTest
 */
public class TestGuiceModule extends AbstractModule {

  @Override
  protected void configure() {

    // Mocked for testing
    bind(CustomerService.class).to(CustomerServiceMock.class);
    bind(OgelService.class).to(OgelServiceMock.class);
  }
}

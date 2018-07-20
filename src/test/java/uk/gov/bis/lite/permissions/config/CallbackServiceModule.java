package uk.gov.bis.lite.permissions.config;

import com.google.inject.AbstractModule;
import uk.gov.bis.lite.permissions.mocks.CustomerServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelServiceMock;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.OgelService;

public class CallbackServiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(CustomerService.class).to(CustomerServiceMock.class);
    bind(OgelService.class).to(OgelServiceMock.class);
  }

}

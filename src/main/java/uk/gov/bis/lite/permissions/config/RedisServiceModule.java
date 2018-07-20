package uk.gov.bis.lite.permissions.config;

import com.google.inject.AbstractModule;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;
import uk.gov.bis.lite.common.redis.RedisCacheModule;
import uk.gov.bis.lite.permissions.service.CallbackService;
import uk.gov.bis.lite.permissions.service.CallbackServiceImpl;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.CustomerServiceImpl;
import uk.gov.bis.lite.permissions.service.LicenceService;
import uk.gov.bis.lite.permissions.service.LicenceServiceImpl;
import uk.gov.bis.lite.permissions.service.OgelService;
import uk.gov.bis.lite.permissions.service.OgelServiceImpl;
import uk.gov.bis.lite.permissions.service.ProcessSubmissionService;
import uk.gov.bis.lite.permissions.service.ProcessSubmissionServiceImpl;
import uk.gov.bis.lite.permissions.service.RedisRegistrationServiceImpl;
import uk.gov.bis.lite.permissions.service.RegisterService;
import uk.gov.bis.lite.permissions.service.RegisterServiceImpl;
import uk.gov.bis.lite.permissions.service.RegistrationService;
import uk.gov.bis.lite.permissions.service.SubmissionService;
import uk.gov.bis.lite.permissions.service.SubmissionServiceImpl;

public class RedisServiceModule extends AbstractModule implements ConfigurationAwareModule<PermissionsAppConfig> {

  private PermissionsAppConfig permissionsAppConfig;

  @Override
  protected void configure() {
    install(new RedisCacheModule(permissionsAppConfig.getRedisConfiguration()));

    bind(CallbackService.class).to(CallbackServiceImpl.class);
    bind(CustomerService.class).to(CustomerServiceImpl.class);
    bind(LicenceService.class).to(LicenceServiceImpl.class);
    bind(OgelService.class).to(OgelServiceImpl.class);
    bind(ProcessSubmissionService.class).to(ProcessSubmissionServiceImpl.class);
    bind(RegisterService.class).to(RegisterServiceImpl.class);
    bind(RegistrationService.class).to(RedisRegistrationServiceImpl.class);
    bind(SubmissionService.class).to(SubmissionServiceImpl.class);
  }

  @Override
  public void setConfiguration(PermissionsAppConfig permissionsAppConfig) {
    this.permissionsAppConfig = permissionsAppConfig;
  }

}

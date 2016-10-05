package uk.gov.bis.lite.permissions.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.skife.jdbi.v2.DBI;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;
import uk.gov.bis.lite.permissions.dao.OgelRegistrationDao;
import uk.gov.bis.lite.permissions.dao.OgelRegistrationDaoImpl;

public class GuiceModule extends AbstractModule implements ConfigurationAwareModule<PermissionsAppConfig> {

  private PermissionsAppConfig config;

  @Override
  protected void configure() {
    bind(OgelRegistrationDao.class).to(OgelRegistrationDaoImpl.class);
  }

  @Provides
  @javax.inject.Named("spireOgelRegistrationsUrl")
  public String provideSpireOgelRegistrationsUrl(PermissionsAppConfig config) {
    return config.getSpireOgelRegistrationsUrl();
  }

  @Provides
  @javax.inject.Named("soapUserName")
  public String provideSpireSiteClientUserName(PermissionsAppConfig config) {
    return config.getSoapUserName();
  }

  @Provides
  @javax.inject.Named("soapPassword")
  public String provideSpireSiteClientPassword(PermissionsAppConfig config) {
    return config.getSoapPassword();
  }

  @Provides
  @Named("jdbi")
  public DBI provideDataSourceJdbi(Environment environment, PermissionsAppConfig config) {
    final DBIFactory factory = new DBIFactory();
    return factory.build(environment, config.getDataSourceFactory(), "sqlite");
  }

  @Provides
  public Scheduler provideScheduler() throws SchedulerException {
    return new StdSchedulerFactory().getScheduler();
  }

  @Override
  public void setConfiguration(PermissionsAppConfig config) {
    this.config = config;
  }

}

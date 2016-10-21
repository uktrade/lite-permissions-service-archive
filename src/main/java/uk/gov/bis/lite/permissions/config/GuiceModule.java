package uk.gov.bis.lite.permissions.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.skife.jdbi.v2.DBI;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationAwareModule;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDaoImpl;
import uk.gov.bis.lite.permissions.spire.SpireService;

import javax.ws.rs.client.Client;

public class GuiceModule extends AbstractModule implements ConfigurationAwareModule<PermissionsAppConfig> {

  private PermissionsAppConfig config;

  @Provides
  @Singleton
  SpireService provideSpireService(Environment environment, PermissionsAppConfig config) {
    SpireService service = new SpireService();
    service.init(config.getSpireUserName(), config.getSpirePassword(), config.getSpireUrl(), config.getSpireEndpoints());
    return service;
  }

  @Provides
  @Singleton
  Client provideHttpClient(Environment environment, PermissionsAppConfig config) {

    // The default timeout is 500ms and often results in Timeout exceptions
    HttpClientConfiguration httpClientConfiguration = new HttpClientConfiguration();
    httpClientConfiguration.setTimeout(Duration.milliseconds(7000));

    HttpClientBuilder clientBuilder = new HttpClientBuilder(environment);
    clientBuilder.using(httpClientConfiguration);

    JerseyClientBuilder builder = new JerseyClientBuilder(environment);
    builder.setApacheHttpClientBuilder(clientBuilder);

    return builder.build("jerseyClient");
  }

  @Override
  protected void configure() {
    bind(OgelSubmissionDao.class).to(OgelSubmissionDaoImpl.class);
  }

  @Provides
  @javax.inject.Named("customerServiceUrl")
  String provideCustomerServiceUrl(PermissionsAppConfig config) {
    return config.getCustomerServiceUrl();
  }

  @Provides
  @javax.inject.Named("customerServiceCustomerPath")
  String provideCustomerServiceCustomerPath(PermissionsAppConfig config) {
    return config.getCustomerServiceCustomerPath();
  }

  @Provides
  @javax.inject.Named("customerServiceSitePath")
  String provideCustomerServiceSitePath(PermissionsAppConfig config) {
    return config.getCustomerServiceSitePath();
  }

  @Provides
  @javax.inject.Named("customerServiceUserRolePath")
  String provideCustomerServiceUserRolePath(PermissionsAppConfig config) {
    return config.getCustomerServiceUserRolePath();
  }

  @Provides
  @javax.inject.Named("maxMinutesRetryAfterFail")
  int provideMaxMinutesRetryAfterFail(PermissionsAppConfig config) {
    return Integer.parseInt(config.getMaxMinutesRetryAfterFail());
  }

  @Provides
  @javax.inject.Named("spireCreateOgelAppUrl")
  String provideSpireCreateOgelAppUrl(PermissionsAppConfig config) {
    return config.getSpireCreateOgelAppUrl();
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

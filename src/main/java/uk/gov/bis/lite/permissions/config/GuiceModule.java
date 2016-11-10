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
import uk.gov.bis.lite.common.spire.client.SpireClientConfig;
import uk.gov.bis.lite.common.spire.client.SpireRequestConfig;
import uk.gov.bis.lite.common.spire.client.parser.ReferenceParser;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDaoImpl;
import uk.gov.bis.lite.permissions.service.RegisterService;
import uk.gov.bis.lite.permissions.service.RegisterServiceImpl;
import uk.gov.bis.lite.permissions.service.SubmissionService;
import uk.gov.bis.lite.permissions.service.SubmissionServiceImpl;
import uk.gov.bis.lite.permissions.spire.SpireReferenceClient;

import javax.ws.rs.client.Client;

public class GuiceModule extends AbstractModule implements ConfigurationAwareModule<PermissionsAppConfig> {

  private PermissionsAppConfig config;

  @Provides
  @Singleton
  SpireReferenceClient provideSpireCreateOgelAppClient(Environment env, PermissionsAppConfig config) {
    return new SpireReferenceClient(
        new ReferenceParser("SPIRE_REF"),
        new SpireClientConfig(config.getSpireClientUserName(), config.getSpireClientPassword(), config.getSpireClientUrl()),
        new SpireRequestConfig("SPIRE_CREATE_OGEL_APP", "OGEL_DETAILS", false));
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
    bind(RegisterService.class).to(RegisterServiceImpl.class);
    bind(SubmissionService.class).to(SubmissionServiceImpl.class);
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
  @javax.inject.Named("customerServiceCustomerNumberPath")
  String provideCustomerServiceCustomerNumberPath(PermissionsAppConfig config) {
    return config.getCustomerServiceCustomerNumberPath();
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

package uk.gov.bis.lite.permissions;

import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.flywaydb.core.Flyway;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller;
import uk.gov.bis.lite.common.jersey.filter.ContainerCorrelationIdFilter;
import uk.gov.bis.lite.permissions.config.GuiceModule;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;
import uk.gov.bis.lite.permissions.resource.OgelRegistrationResource;
import uk.gov.bis.lite.permissions.resource.OgelSubmissionResource;
import uk.gov.bis.lite.permissions.resource.RegisterOgelResource;
import uk.gov.bis.lite.permissions.scheduler.Scheduler;
import uk.gov.bis.lite.permissions.util.SimpleAuthenticator;

public class PermissionsApp extends Application<PermissionsAppConfig> {

  private GuiceBundle<PermissionsAppConfig> guiceBundle;
  private final Module module;

  public PermissionsApp() {
    this(new GuiceModule());
  }

  public PermissionsApp(Module module) {
    super();
    this.module = module;
  }

  @Override
  public void initialize(Bootstrap<PermissionsAppConfig> bootstrap) {
    guiceBundle = new GuiceBundle.Builder<PermissionsAppConfig>()
        .modules(module)
        .installers(ResourceInstaller.class, ManagedInstaller.class)
        .extensions(RegisterOgelResource.class, OgelRegistrationResource.class, OgelSubmissionResource.class, Scheduler.class)
        .build();
    bootstrap.addBundle(guiceBundle);
  }

  @Override
  public void run(PermissionsAppConfig config, Environment environment) throws Exception {

    environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<PrincipalImpl>()
        .setAuthenticator(new SimpleAuthenticator(config.getAdminLogin(), config.getAdminPassword()))
        .setRealm("Permissions Service Admin Authentication")
        .buildAuthFilter()));

    environment.jersey().register(ContainerCorrelationIdFilter.class);

    // Perform/validate flyway migration on startup
    DataSourceFactory dataSourceFactory = config.getDataSourceFactory();
    Flyway flyway = new Flyway();
    flyway.setDataSource(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword());
    flyway.migrate();

  }

  public GuiceBundle<PermissionsAppConfig> getGuiceBundle() {
    return guiceBundle;
  }

  public static void main(String[] args) throws Exception {
    new PermissionsApp().run(args);
  }
}


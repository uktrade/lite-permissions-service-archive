package uk.gov.bis.lite.permissions;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.flywaydb.core.Flyway;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller;
import uk.gov.bis.lite.permissions.config.GuiceModule;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;
import uk.gov.bis.lite.permissions.resource.RegisterOgelResource;
import uk.gov.bis.lite.permissions.scheduler.Scheduler;

public class PermissionsApp extends Application<PermissionsAppConfig> {

  private GuiceBundle<PermissionsAppConfig> guiceBundle;

  @Override
  public void initialize(Bootstrap<PermissionsAppConfig> bootstrap) {
    guiceBundle = new GuiceBundle.Builder<PermissionsAppConfig>()
        .modules(new GuiceModule())
        .installers(ResourceInstaller.class, ManagedInstaller.class)
        .extensions(RegisterOgelResource.class, Scheduler.class)
        .build();
    bootstrap.addBundle(guiceBundle);
  }

  @Override
  public void run(PermissionsAppConfig config, Environment environment) throws Exception {

    // Perform/validate flyway migration on startup
    DataSourceFactory dataSourceFactory = config.getDataSourceFactory();
    Flyway flyway = new Flyway();
    flyway.setDataSource(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword());
    flyway.migrate();

  }

  public static void main(String[] args) throws Exception {
    new PermissionsApp().run(args);
  }
}


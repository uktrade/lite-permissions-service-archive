package uk.gov.bis.lite.permissions;

import com.codahale.metrics.servlets.AdminServlet;
import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.auth.PolymorphicAuthDynamicFeature;
import io.dropwizard.auth.PolymorphicAuthValueFactoryProvider;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.flywaydb.core.Flyway;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.installer.feature.ManagedInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller;
import uk.gov.bis.lite.common.auth.admin.AdminConstraintSecurityHandler;
import uk.gov.bis.lite.common.jersey.filter.ContainerCorrelationIdFilter;
import uk.gov.bis.lite.common.jwt.LiteJwtAuthFilterHelper;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.common.paas.db.CloudFoundryEnvironmentSubstitutor;
import uk.gov.bis.lite.permissions.config.GuiceModule;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;
import uk.gov.bis.lite.permissions.resource.LicenceResource;
import uk.gov.bis.lite.permissions.resource.OgelRegistrationResource;
import uk.gov.bis.lite.permissions.resource.OgelSubmissionResource;
import uk.gov.bis.lite.permissions.resource.RegisterOgelResource;
import uk.gov.bis.lite.permissions.scheduler.ProcessSubmissionScheduler;
import uk.gov.bis.lite.permissions.util.SimpleAuthenticator;

public class PermissionsApp extends Application<PermissionsAppConfig> {

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
    // Load config from a resource (i.e. file within the JAR), and substitute environment variables into it
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            new ResourceConfigurationSourceProvider(), new CloudFoundryEnvironmentSubstitutor()));

    GuiceBundle<PermissionsAppConfig> guiceBundle = new GuiceBundle.Builder<PermissionsAppConfig>()
        .modules(module)
        .installers(ResourceInstaller.class, ManagedInstaller.class)
        .extensions(RegisterOgelResource.class, OgelRegistrationResource.class, OgelSubmissionResource.class,
            LicenceResource.class, ProcessSubmissionScheduler.class)
        .build();
    bootstrap.addBundle(guiceBundle);
  }

  @Override
  public void run(PermissionsAppConfig config, Environment environment) throws Exception {

    String jwtSharedSecret = config.getJwtSharedSecret();

    BasicCredentialAuthFilter<PrincipalImpl> basicAuthFilter = new BasicCredentialAuthFilter.Builder<PrincipalImpl>()
        .setAuthenticator(new SimpleAuthenticator(config.getAdminLogin(), config.getAdminPassword()))
        .setRealm("Permissions Service Admin Authentication")
        .buildAuthFilter();

    JwtAuthFilter<LiteJwtUser> liteJwtUserJwtAuthFilter = LiteJwtAuthFilterHelper.buildAuthFilter(jwtSharedSecret);

    PolymorphicAuthDynamicFeature authFeature = new PolymorphicAuthDynamicFeature<>(
        ImmutableMap.of(PrincipalImpl.class, basicAuthFilter, LiteJwtUser.class, liteJwtUserJwtAuthFilter));

    AbstractBinder authBinder = new PolymorphicAuthValueFactoryProvider.Binder<>(
        ImmutableSet.of(PrincipalImpl.class, LiteJwtUser.class));

    environment.jersey().register(authFeature);
    environment.jersey().register(authBinder);
    environment.jersey().register(ContainerCorrelationIdFilter.class);

    environment.admin().addServlet("admin", new AdminServlet()).addMapping("/admin");
    environment.admin().setSecurityHandler(new AdminConstraintSecurityHandler(config.getServiceLogin(), config.getServicePassword()));
    //environment.admin().setSecurityHandler(new AdminConstraintSecurityHandler(config.getLogin(), config.getPassword()));

    // Perform/validate flyway migration on startup
    flywayMigrate(config);
  }

  protected void flywayMigrate(PermissionsAppConfig config) {
    DataSourceFactory dataSourceFactory = config.getDataSourceFactory();
    Flyway flyway = new Flyway();
    flyway.setDataSource(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword());
    flyway.migrate();
  }

  public static void main(String[] args) throws Exception {
    new PermissionsApp().run(args);
  }
}


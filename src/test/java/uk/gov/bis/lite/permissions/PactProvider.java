package uk.gov.bis.lite.permissions;

import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;

@RunWith(PactRunner.class)
@Provider("lite-permissions-service")
@PactFolder("//Users//Tomacpro//Projects//GitHub//lite-ogel-registration//target//pacts")
public class PactProvider {

  @ClassRule
  public static final DropwizardAppRule<PermissionsAppConfig> RULE =
    new DropwizardAppRule<>(TestCustomerApplication.class, "/Users/Tomacpro/Projects/GitHub/lite-permissions-service/src/test/resources/service-test.yaml");

  @TestTarget // Annotation denotes Target that will be used for tests
  public final Target target = new HttpTarget(RULE.getLocalPort()); // Out-of-the-box implementation of Target (for more information take a look at Test Target section)


  @BeforeClass
  public static void before() {
    Flyway flyway = new Flyway();
    DataSourceFactory dsf = RULE.getConfiguration().getDataSourceFactory();
    flyway.setDataSource(dsf.getUrl(), dsf.getUser(), dsf.getPassword());
    flyway.migrate();
  }
}


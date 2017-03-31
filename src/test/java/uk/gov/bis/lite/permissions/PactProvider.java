package uk.gov.bis.lite.permissions;

import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.loader.PactBroker;
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

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@RunWith(PactRunner.class)
@Provider("lite-permissions-service")
@PactBroker(host = "pact-broker.mgmt.licensing.service.trade.gov.uk.test", port = "80")
public class PactProvider {

  @ClassRule
  public static final DropwizardAppRule<PermissionsAppConfig> RULE =
    new DropwizardAppRule<>(TestCustomerApplication.class, resourceFilePath("service-test.yaml"));

  @TestTarget
  public final Target target = new HttpTarget(RULE.getLocalPort());

  @BeforeClass
  public static void before() {
    Flyway flyway = new Flyway();
    DataSourceFactory dsf = RULE.getConfiguration().getDataSourceFactory();
    flyway.setDataSource(dsf.getUrl(), dsf.getUser(), dsf.getPassword());
    flyway.migrate();
  }
}
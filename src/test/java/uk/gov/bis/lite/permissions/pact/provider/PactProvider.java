package uk.gov.bis.lite.permissions.pact.provider;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import uk.gov.bis.lite.permissions.PactPermissionsApp;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;
import uk.gov.bis.lite.permissions.mocks.pact.RegistrationsServiceMock;

@RunWith(PactRunner.class)
@Provider("lite-permissions-service")
@PactBroker(host = "pact-broker.mgmt.licensing.service.trade.gov.uk.test", port = "80")
public class PactProvider {

  @ClassRule
  public static final DropwizardAppRule<PermissionsAppConfig> RULE =
    new DropwizardAppRule<>(PactPermissionsApp.class, resourceFilePath("service-test.yaml"));

  @TestTarget
  public final Target target = new HttpTarget(RULE.getLocalPort());

  @State("OGEL registrations exist for provided user")
  public void someRegistrationsState() {
    InjectorLookup.getInjector(RULE.getApplication()).get().getInstance(RegistrationsServiceMock.class).setHasRegistrations(true);
  }

  @State("no OGEL registrations exist for provided user")
  public void noRegistrationsState() {
    InjectorLookup.getInjector(RULE.getApplication()).get().getInstance(RegistrationsServiceMock.class).setHasRegistrations(false);
  }
}
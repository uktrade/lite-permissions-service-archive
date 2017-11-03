package uk.gov.bis.lite.permissions.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import uk.gov.bis.lite.permissions.PermissionsApp;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;

public class BaseIntegrationTest {

  /**
   * Configured to bind port dynamically to mitigate bug https://github.com/tomakehurst/wiremock/issues/97
   * TODO revert to static port once bug is resolved
   */
  @ClassRule
  public static final WireMockClassRule wireMockClassRule = new WireMockClassRule(options().dynamicPort());

  @Rule
  public final DropwizardAppRule<PermissionsAppConfig> RULE =
      new DropwizardAppRule<>(PermissionsApp.class, resourceFilePath("service-test.yaml"),
          ConfigOverride.config("customerServiceUrl", "http://localhost:" +  wireMockClassRule.port() + "/"),
          ConfigOverride.config("spireClientUrl", "http://localhost:" +  wireMockClassRule.port() + "/spire/fox/ispire/"));

  @Before
  public void setupWireMockStubFor() {
    configureFor(wireMockClassRule.port());
  }

  @Before
  public void setupDatabase() {
    DataSourceFactory f = RULE.getConfiguration().getDataSourceFactory();
    Flyway flyway = new Flyway();
    flyway.setDataSource(f.getUrl(), f.getUser(), f.getPassword());
    flyway.migrate();
  }

  String localUrl(String target) {
    return "http://localhost:" + RULE.getLocalPort() + target;
  }
}

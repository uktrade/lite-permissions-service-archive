package uk.gov.bis.lite.permissions.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_5;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import uk.gov.bis.lite.common.paas.db.SchemaAwareDataSourceFactory;
import uk.gov.bis.lite.permissions.PermissionsApp;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;

public class BaseIntegrationTest {

  private static EmbeddedPostgres postgres;

  /**
   * Configured to bind port dynamically to mitigate bug https://github.com/tomakehurst/wiremock/issues/97
   * TODO revert to static port once bug is resolved
   */
  @ClassRule
  public static final WireMockClassRule wireMockClassRule = new WireMockClassRule(options().dynamicPort());

  public DropwizardAppRule<PermissionsAppConfig> RULE;

  private Flyway flyway;

  @BeforeClass
  public static void beforeClass() throws Exception {
    postgres = new EmbeddedPostgres(V9_5);
    postgres.start("localhost", 5432, "dbName", "postgres", "password");
  }

  @AfterClass
  public static void afterClass() {
    postgres.stop();
  }

  @Before
  public void before() {
    // Setup WireMock StubFor
    configureFor(wireMockClassRule.port());

    RULE = new DropwizardAppRule<>(PermissionsApp.class, "service-test.yaml",
        ConfigOverride.config("customerServiceUrl", "http://localhost:" +  wireMockClassRule.port() + "/"),
        ConfigOverride.config("spireClientUrl", "http://localhost:" +  wireMockClassRule.port() + "/spire/fox/ispire/"));
    RULE.getTestSupport().before();

    SchemaAwareDataSourceFactory dataSourceFactory = RULE.getConfiguration().getDataSourceFactory();
    flyway = new Flyway();
    flyway.setDataSource(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword());
    flyway.migrate();
  }

  @After
  public void after() {
    RULE.getTestSupport().after();
    flyway.clean();
  }

  String localUrl(String target) {
    return "http://localhost:" + RULE.getLocalPort() + target;
  }

}

package uk.gov.bis.lite.permissions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_5;

import com.google.inject.Injector;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import uk.gov.bis.lite.common.paas.db.SchemaAwareDataSourceFactory;
import uk.gov.bis.lite.permissions.TestPermissionsApp;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.mocks.CustomerServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelServiceMock;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

/**
 * Integration tests for ProcessSubmissionService with mocked CustomerService and OgelService Utilises in memory DB
 */
public class ProcessSubmissionServiceTest {

  private static ProcessSubmissionService processSubmissionService;
  private static OgelSubmissionDao submissionDao;
  private static CustomerServiceMock customerServiceMock;
  private static OgelServiceMock ogelServiceMock;
  private static EmbeddedPostgres postgres;
  private static Flyway flyway;

  private static DropwizardAppRule<PermissionsAppConfig> APP_RULE;

  @BeforeClass
  public static void beforeClass() throws Exception {
    postgres = new EmbeddedPostgres(V9_5);
    postgres.start("localhost", 5432, "dbName", "postgres", "password");

    APP_RULE = new DropwizardAppRule<>(TestPermissionsApp.class, "test-config.yaml");
    APP_RULE.getTestSupport().before();

    SchemaAwareDataSourceFactory dataSourceFactory = APP_RULE.getConfiguration().getDataSourceFactory();
    flyway = new Flyway();
    flyway.setDataSource(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword());

    Injector injector = InjectorLookup.getInjector(APP_RULE.getApplication()).get();
    processSubmissionService = injector.getInstance(ProcessSubmissionService.class);
    submissionDao = injector.getInstance(OgelSubmissionDao.class);
    customerServiceMock = (CustomerServiceMock) injector.getInstance(CustomerService.class);
    ogelServiceMock = (OgelServiceMock) injector.getInstance(OgelService.class);
  }

  @AfterClass
  public static void afterClass() throws Exception {
    APP_RULE.getTestSupport().after();
    postgres.stop();
  }

  @Before
  public void setUp() throws Exception {
    flyway.migrate();
  }

  @After
  public void tearDown() throws Exception {
    flyway.clean();
  }

  @Test
  public void runSuccessAll() {

    String SUB_REF = "SuccessAll";

    // Setup
    resetAllMocks();
    submissionDao.create(Util.getMockSubmission(SUB_REF));

    // Process OgelSubmission
    processSubmissionService.doProcessOgelSubmission(submissionDao.findBySubmissionRef(SUB_REF));

    // Check OgelSubmission Stage and Status
    OgelSubmission sub = submissionDao.findBySubmissionRef(SUB_REF);
    assertThat(sub.getStatus()).isEqualTo(Util.STATUS_COMPLETE);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_OGEL);

    // Check call counts to mocked services
    assertEquals(1, customerServiceMock.getCustomerCallCount());
    assertEquals(1, customerServiceMock.getSiteCallCount());
    assertEquals(1, customerServiceMock.getUserRoleCallCount());
    assertEquals(1, ogelServiceMock.getCreateOgelCallCount());
    assertEquals(1, submissionDao.getScheduledCompleteToCallback().size());
  }

  @Test
  public void runFailOnCustomer() {

    String SUB_REF = "FailOnCustomer";

    // Setup
    resetAllMocks();
    customerServiceMock.setCreateCustomerSuccess(false);
    submissionDao.create(Util.getMockSubmission(SUB_REF));

    // Process OgelSubmission
    processSubmissionService.doProcessOgelSubmission(submissionDao.findBySubmissionRef(SUB_REF));

    // Check OgelSubmission Stage and Status
    OgelSubmission sub = submissionDao.findBySubmissionRef(SUB_REF);
    assertThat(sub.getStatus()).isEqualTo(Util.STATUS_ACTIVE);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_CUSTOMER);

    // Check call counts to mocked services
    assertEquals(1, customerServiceMock.getCustomerCallCount());
    assertEquals(0, customerServiceMock.getSiteCallCount());
    assertEquals(0, customerServiceMock.getUserRoleCallCount());
    assertEquals(0, ogelServiceMock.getCreateOgelCallCount());
  }

  @Test
  public void runFailOnSite() {

    String SUB_REF = "FailOnSite";

    // Setup
    resetAllMocks();
    customerServiceMock.setCreateSiteSuccess(false);
    submissionDao.create(Util.getMockSubmission(SUB_REF));

    // Process OgelSubmission
    processSubmissionService.doProcessOgelSubmission(submissionDao.findBySubmissionRef(SUB_REF));

    // Check OgelSubmission Stage and Status
    OgelSubmission sub = submissionDao.findBySubmissionRef(SUB_REF);
    assertThat(sub.getStatus()).isEqualTo(Util.STATUS_ACTIVE);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_SITE);

    // Check call counts to mocked services
    assertEquals(1, customerServiceMock.getCustomerCallCount());
    assertEquals(1, customerServiceMock.getSiteCallCount());
    assertEquals(0, customerServiceMock.getUserRoleCallCount());
    assertEquals(0, ogelServiceMock.getCreateOgelCallCount());
  }

  @Test
  public void runFailOnUserRole() {

    String SUB_REF = "FailOnUserRole";

    // Setup
    resetAllMocks();
    customerServiceMock.setUpdateUserRoleSuccess(false);
    submissionDao.create(Util.getMockSubmission(SUB_REF));

    // Process OgelSubmission
    processSubmissionService.doProcessOgelSubmission(submissionDao.findBySubmissionRef(SUB_REF));

    // Check OgelSubmission Stage and Status
    OgelSubmission sub = submissionDao.findBySubmissionRef(SUB_REF);
    assertThat(sub.getStatus()).isEqualTo(Util.STATUS_ACTIVE);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_USER_ROLE);

    // Check call counts to mocked services
    assertEquals(1, customerServiceMock.getCustomerCallCount());
    assertEquals(1, customerServiceMock.getSiteCallCount());
    assertEquals(1, customerServiceMock.getUserRoleCallCount());
    assertEquals(0, ogelServiceMock.getCreateOgelCallCount());
  }

  @Test
  public void runFailOnOgel() {

    String SUB_REF = "FailOnOgel";

    // Setup
    resetAllMocks();
    ogelServiceMock.setCreateOgelSuccess(false);
    submissionDao.create(Util.getMockSubmission(SUB_REF));

    // Process OgelSubmission
    processSubmissionService.doProcessOgelSubmission(submissionDao.findBySubmissionRef(SUB_REF));

    // Check OgelSubmission Stage and Status
    OgelSubmission sub = submissionDao.findBySubmissionRef(SUB_REF);
    assertThat(sub.getStatus()).isEqualTo(Util.STATUS_ACTIVE);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_OGEL);

    // Check call counts to mocked services
    assertEquals(1, customerServiceMock.getCustomerCallCount());
    assertEquals(1, customerServiceMock.getSiteCallCount());
    assertEquals(1, customerServiceMock.getUserRoleCallCount());
    assertEquals(1, ogelServiceMock.getCreateOgelCallCount());
  }


  /**
   * Sets all mock calls to respond with a 'success' Resets all mock call counts to 0
   */
  private void resetAllMocks() {
    customerServiceMock.setAllSuccess(true);
    ogelServiceMock.setCreateOgelSuccess(true);
    customerServiceMock.resetAllCounts();
    ogelServiceMock.resetCreateOgelCallCount();
  }

}

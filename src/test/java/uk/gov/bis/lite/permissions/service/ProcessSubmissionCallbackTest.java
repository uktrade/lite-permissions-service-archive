package uk.gov.bis.lite.permissions.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import uk.gov.bis.lite.permissions.model.FailEvent;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.List;

/**
 * Integration tests for ProcessSubmissionService with mocked CustomerService and OgelService Testing fail to callback
 * logic, and terminations Utilises in memory DB
 */
public class ProcessSubmissionCallbackTest {

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
    ogelServiceMock = (OgelServiceMock) injector.getInstance(OgelService.class);
    processSubmissionService = injector.getInstance(ProcessSubmissionService.class);
    submissionDao = injector.getInstance(OgelSubmissionDao.class);
    customerServiceMock = (CustomerServiceMock) injector.getInstance(CustomerService.class);
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
  public void runSuccessToCallback() {

    String SUB_REF = "SuccessToCallback";

    // Setup
    resetAllMocks();
    submissionDao.create(Util.getMockSubmission(SUB_REF));

    // Process OgelSubmission
    processSubmissionService.doProcessOgelSubmission(findBy(SUB_REF));

    // Test Submission is in Callbacks
    assertThat(findCallbacks()).extracting(OgelSubmission::getSubmissionRef).contains(SUB_REF);
  }

  @Test
  public void runCustomerPermissionDeniedToCallback() {

    String SUB_REF = "CustomerPermissionDeniedToCallback";

    // Setup
    resetAllMocks();
    customerServiceMock.setCreateCustomerSuccess(false);
    customerServiceMock.setFailEvent(new FailEvent(Util.PERMISSION_DENIED, Util.ORIGIN_CUSTOMER, Util.ERROR_MESSAGE));
    submissionDao.create(Util.getMockSubmission(SUB_REF));

    // Process OgelSubmission
    processSubmissionService.doProcessOgelSubmission(findBy(SUB_REF));

    // Test Submission is in Callbacks
    assertThat(findCallbacks()).extracting(OgelSubmission::getSubmissionRef).contains(SUB_REF);
  }

  @Test
  public void runOgelFailReasonToCallback() {

    String SUB_REF1 = "OgelPermissionDeniedToCallback";
    String SUB_REF2 = "OgelBlacklistedToCallback";
    String SUB_REF3 = "OgelSiteAlreadyRegisteredToCallback";

    // Setup and process
    resetAllMocks();
    ogelServiceMock.setCreateOgelSuccess(false);
    ogelServiceMock.setFailEvent(new FailEvent(Util.PERMISSION_DENIED, Util.ORIGIN_OGEL_CREATE, Util.ERROR_MESSAGE));
    submissionDao.create(Util.getMockSubmission(SUB_REF1));
    processSubmissionService.doProcessOgelSubmission(findBy(SUB_REF1));

    resetAllMocks();
    ogelServiceMock.setCreateOgelSuccess(false);
    ogelServiceMock.setFailEvent(new FailEvent(Util.BLACKLISTED, Util.ORIGIN_OGEL_CREATE, Util.ERROR_MESSAGE));
    submissionDao.create(Util.getMockSubmission(SUB_REF2));
    processSubmissionService.doProcessOgelSubmission(findBy(SUB_REF2));

    resetAllMocks();
    ogelServiceMock.setCreateOgelSuccess(false);
    ogelServiceMock.setFailEvent(new FailEvent(Util.SITE_ALREADY_REGISTERED, Util.ORIGIN_OGEL_CREATE, Util.ERROR_MESSAGE));
    submissionDao.create(Util.getMockSubmission(SUB_REF3));
    processSubmissionService.doProcessOgelSubmission(findBy(SUB_REF3));

    // Test Submissions are in Callbacks
    assertThat(findCallbacks()).extracting(OgelSubmission::getSubmissionRef).contains(SUB_REF1);
    assertThat(findCallbacks()).extracting(OgelSubmission::getSubmissionRef).contains(SUB_REF2);
    assertThat(findCallbacks()).extracting(OgelSubmission::getSubmissionRef).contains(SUB_REF3);
  }

  @Test
  public void runSitePermissionDeniedToCallback() {

    String SUB_REF = "SitePermissionDeniedToCallback";

    // Setup
    resetAllMocks();
    customerServiceMock.setCreateSiteSuccess(false);
    customerServiceMock.setFailEvent(new FailEvent(Util.PERMISSION_DENIED, Util.ORIGIN_SITE, Util.ERROR_MESSAGE));
    submissionDao.create(Util.getMockSubmission(SUB_REF));

    // Process OgelSubmission
    processSubmissionService.doProcessOgelSubmission(findBy(SUB_REF));

    // Test Submission is in Callbacks
    assertThat(findCallbacks()).extracting(OgelSubmission::getSubmissionRef).contains(SUB_REF);

  }

  @Test
  public void runUserRoleRepeatingFailToCompleted() {

    String SUB_REF = "runUserRoleRepeatingFailToCompleted";

    // Setup
    resetAllMocks();
    customerServiceMock.setUpdateUserRoleSuccess(false);
    customerServiceMock.setFailEvent(new FailEvent(Util.ENDPOINT_ERROR, Util.ORIGIN_USER_ROLE, Util.ERROR_MESSAGE));
    submissionDao.create(Util.getMockSubmission(SUB_REF));

    // Process OgelSubmission
    processSubmissionService.doProcessOgelSubmission(findBy(SUB_REF));

    // Test Submission is NOT in Callbacks
    assertThat(findCallbacks()).extracting(OgelSubmission::getSubmissionRef).doesNotContain(SUB_REF);

    // Process OgelSubmission
    processSubmissionService.doProcessOgelSubmission(findBy(SUB_REF));

    // Test Submission is COMPLETED
    assertThat(findBy(SUB_REF).getStatus()).isEqualTo(Util.STATUS_COMPLETE);

    // Test Submission is in Callbacks
    assertThat(findCallbacks()).extracting(OgelSubmission::getSubmissionRef).contains(SUB_REF);
  }

  /**
   * Sets all mock calls to respond with a 'success' Resets all mock call counts to 0
   */
  private void resetAllMocks() {
    customerServiceMock.setAllSuccess(true);
    customerServiceMock.resetAllCounts();
    customerServiceMock.resetFailEvent();

    ogelServiceMock.setCreateOgelSuccess(true);
    ogelServiceMock.resetCreateOgelCallCount();
    ogelServiceMock.resetFailEvent();
  }

  private List<OgelSubmission> findCallbacks() {
    return submissionDao.getScheduledCompleteToCallback();
  }

  private OgelSubmission findBy(String subRef) {
    return submissionDao.findBySubmissionRef(subRef);
  }

}

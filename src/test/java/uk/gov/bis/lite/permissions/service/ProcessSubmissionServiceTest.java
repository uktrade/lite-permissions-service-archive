package uk.gov.bis.lite.permissions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.PermissionsTestApp;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.mocks.CustomerServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelServiceMock;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

/**
 * Integration tests for ProcessSubmissionService with mocked CustomerService and OgelService
 * Utilises in memory DB
 */
public class ProcessSubmissionServiceTest {

  private static ProcessSubmissionService processSubmissionService;
  private static OgelSubmissionDao submissionDao;
  private static CustomerServiceMock customerServiceMock;
  private static OgelServiceMock ogelServiceMock;

  @ClassRule
  public static final DropwizardAppRule<PermissionsAppConfig> APP_RULE = new DropwizardAppRule<>(PermissionsTestApp.class,
      ResourceHelpers.resourceFilePath("test-config.yaml"));

  @BeforeClass
  public static void before() {
    Flyway flyway = new Flyway();
    DataSourceFactory dsf = APP_RULE.getConfiguration().getDataSourceFactory();
    flyway.setDataSource(dsf.getUrl(), dsf.getUser(), dsf.getPassword());
    flyway.migrate();

    PermissionsTestApp app = APP_RULE.getApplication();
    processSubmissionService = app.getInstance(ProcessSubmissionService.class);
    submissionDao = app.getInstance(OgelSubmissionDao.class);
    customerServiceMock = (CustomerServiceMock) app.getInstance(CustomerService.class);
    ogelServiceMock = (OgelServiceMock) app.getInstance(OgelService.class);
  }

  @Test
  public void runSuccessAll() {

    String SUB_REF = "SuccessAll";

    // Setup
    resetAllMocks(true);
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
    resetAllMocks(true);
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
    resetAllMocks(true);
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
    resetAllMocks(true);
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
    resetAllMocks(true);
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
   * Sets all mock calls to respond with a 'success'
   * Resets all mock call counts to 0
   */
  private void resetAllMocks(boolean arg) {
    customerServiceMock.setAllSuccess(arg);
    ogelServiceMock.setCreateOgelSuccess(arg);
    customerServiceMock.resetAllCounts();
    ogelServiceMock.resetCreateOgelCallCount();
  }

}

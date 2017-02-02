package uk.gov.bis.lite.permissions.submissions;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.PermissionsTestApp;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.mocks.CustomerServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelServiceMock;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.OgelService;
import uk.gov.bis.lite.permissions.service.ProcessOgelSubmissionService;

import java.io.IOException;

public class ProcessSubmissionsTest {

  private ProcessOgelSubmissionService processOgelSubmissionService;
  private OgelSubmissionDao dao;
  private CustomerServiceMock customerServiceMock;
  private OgelServiceMock ogelServiceMock;

  private String SUB_REF = "SUB_REF";

  @ClassRule
  public static final DropwizardAppRule<PermissionsAppConfig> APP_RULE = new DropwizardAppRule<>(PermissionsTestApp.class,
      ResourceHelpers.resourceFilePath("test-config.yaml"));

  @Before
  public void before() {
    Flyway flyway = new Flyway();
    DataSourceFactory dsf = APP_RULE.getConfiguration().getDataSourceFactory();
    flyway.setDataSource(dsf.getUrl(), dsf.getUser(), dsf.getPassword());
    flyway.migrate();

    PermissionsTestApp app = APP_RULE.getApplication();
    processOgelSubmissionService = app.getInstance(ProcessOgelSubmissionService.class);

    dao = app.getInstance(OgelSubmissionDao.class);

    customerServiceMock = (CustomerServiceMock)app.getInstance(CustomerService.class);
    ogelServiceMock = (OgelServiceMock)app.getInstance(OgelService.class);
  }

  @Test
  public void runSubmissionTest() {

    // Setup
    createMockedSubmission(SUB_REF);
    resetMocksForSuccess(true);
    assertThat(dao.getScheduledActive()).hasSize(1);

    // Check initial Stage and State
    OgelSubmission initialSub = dao.findRecentBySubmissionRef(SUB_REF);
    assertThat(initialSub.getStage()).isEqualTo(OgelSubmission.Stage.CREATED);
    assertThat(initialSub.getStatus()).isEqualTo(OgelSubmission.Status.ACTIVE);

    // Process OgelSubmission to fail on SITE
    customerServiceMock.setCreateSiteSuccess(false);
    processOgelSubmissionService.doProcessOgelSubmission(initialSub);

    // Check Stage and State
    OgelSubmission siteSub = dao.findRecentBySubmissionRef(SUB_REF);
    assertThat(siteSub.getStage()).isEqualTo(OgelSubmission.Stage.SITE);
    assertThat(siteSub.getStatus()).isEqualTo(OgelSubmission.Status.ACTIVE);

    // Process OgelSubmission to fail on USER_ROLE
    customerServiceMock.setCreateSiteSuccess(true);
    customerServiceMock.setUpdateUserRoleSuccess(false);
    processOgelSubmissionService.doProcessOgelSubmission(siteSub);

    // Check Stage and State
    OgelSubmission userRoleSub = dao.findRecentBySubmissionRef(SUB_REF);
    assertThat(userRoleSub.getStage()).isEqualTo(OgelSubmission.Stage.USER_ROLE);
    assertThat(userRoleSub.getStatus()).isEqualTo(OgelSubmission.Status.ACTIVE);

    // Process OgelSubmission to OGEL
    resetMocksForSuccess(true);
    processOgelSubmissionService.doProcessOgelSubmission(userRoleSub);

    // Check Stage and State
    OgelSubmission ogelSub = dao.findRecentBySubmissionRef(SUB_REF);
    assertThat(ogelSub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);
    assertThat(ogelSub.getStatus()).isEqualTo(OgelSubmission.Status.COMPLETE);

    assertThat(dao.getScheduledActive()).hasSize(0);
    assertThat(dao.getScheduledCompleteToCallback()).hasSize(1);
    assertThat(dao.getPendingSubmissions()).hasSize(1);
    assertThat(dao.getCancelledSubmissions()).hasSize(0);
    assertThat(dao.getFinishedSubmissions()).hasSize(0);
  }

  private void createMockedSubmission(String submissionRef) {
    OgelSubmission sub = new OgelSubmission("24492", "24492");
    sub.setScheduledMode();
    sub.setSubmissionRef(submissionRef);
    sub.setRoleUpdate(true);
    dao.create(sub);
  }

  private void resetMocksForSuccess(boolean arg) {
    customerServiceMock.setAllSuccess(arg);
    ogelServiceMock.setCreateOgelSuccess(arg);
  }


  @After
  public void after() throws IOException {
    //Files.deleteIfExists(Paths.get("test.localPermissions.db"));
  }

}

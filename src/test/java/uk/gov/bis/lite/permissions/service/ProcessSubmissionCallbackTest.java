package uk.gov.bis.lite.permissions.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Injector;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
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

  @ClassRule
  public static final DropwizardAppRule<PermissionsAppConfig> APP_RULE = new DropwizardAppRule<>(TestPermissionsApp.class,
      ResourceHelpers.resourceFilePath("test-config.yaml"));

  @BeforeClass
  public static void before() {
    Flyway flyway = new Flyway();
    DataSourceFactory dsf = APP_RULE.getConfiguration().getDataSourceFactory();
    flyway.setDataSource(dsf.getUrl(), dsf.getUser(), dsf.getPassword());
    flyway.migrate();

    Injector injector = InjectorLookup.getInjector(APP_RULE.getApplication()).get();
    ogelServiceMock = (OgelServiceMock) injector.getInstance(OgelService.class);
    processSubmissionService = injector.getInstance(ProcessSubmissionService.class);
    submissionDao = injector.getInstance(OgelSubmissionDao.class);
    customerServiceMock = (CustomerServiceMock) injector.getInstance(CustomerService.class);
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

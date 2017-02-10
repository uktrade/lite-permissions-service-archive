package uk.gov.bis.lite.permissions.dao;

import static org.assertj.core.api.Assertions.assertThat;

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
import uk.gov.bis.lite.permissions.model.OgelSubmission;

/**
 * Integration test for OgelSubmissionDao
 */
public class SubmissionDaoTest {

  private static OgelSubmissionDao submissionDao;

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
    submissionDao = app.getInstance(OgelSubmissionDao.class);
  }

  @Test
  public void runSubmissionDaoTest() {

    submissionDao.create(getScheduled(Util.STAGE_CREATED));

    assertThat(submissionDao.getScheduledActive()).hasSize(1);
    assertThat(submissionDao.getScheduledCompleteToCallback()).hasSize(0);
    assertThat(submissionDao.getPendingSubmissions()).hasSize(1);
    assertThat(submissionDao.getCancelledSubmissions()).hasSize(0);
    assertThat(submissionDao.getFinishedSubmissions()).hasSize(0);

    submissionDao.create(getScheduled(Util.STAGE_CUSTOMER));

    assertThat(submissionDao.getScheduledActive()).hasSize(2);
    assertThat(submissionDao.getScheduledCompleteToCallback()).hasSize(0);
    assertThat(submissionDao.getPendingSubmissions()).hasSize(2);
    assertThat(submissionDao.getCancelledSubmissions()).hasSize(0);
    assertThat(submissionDao.getFinishedSubmissions()).hasSize(0);

    OgelSubmission forComplete = getScheduled(Util.STAGE_OGEL);
    forComplete.setStatus(Util.STATUS_COMPLETE);
    submissionDao.create(forComplete);

    assertThat(submissionDao.getScheduledActive()).hasSize(2);
    assertThat(submissionDao.getScheduledCompleteToCallback()).hasSize(1);
    assertThat(submissionDao.getPendingSubmissions()).hasSize(3);
    assertThat(submissionDao.getCancelledSubmissions()).hasSize(0);
    assertThat(submissionDao.getFinishedSubmissions()).hasSize(0);

    OgelSubmission forCancel = getScheduled(Util.STAGE_CREATED);
    forCancel.setStatus(Util.STATUS_TERMINATED);
    submissionDao.create(forCancel);

    assertThat(submissionDao.getScheduledActive()).hasSize(2);
    assertThat(submissionDao.getScheduledCompleteToCallback()).hasSize(1);
    assertThat(submissionDao.getPendingSubmissions()).hasSize(3);
    assertThat(submissionDao.getCancelledSubmissions()).hasSize(1);
    assertThat(submissionDao.getFinishedSubmissions()).hasSize(0);

    OgelSubmission forFinished = getScheduled(Util.STAGE_OGEL);
    forFinished.setStatus(Util.STATUS_COMPLETE);
    forFinished.setCalledBack(true);
    submissionDao.create(forFinished);

    assertThat(submissionDao.getScheduledActive()).hasSize(2);
    assertThat(submissionDao.getScheduledCompleteToCallback()).hasSize(1);
    assertThat(submissionDao.getPendingSubmissions()).hasSize(3);
    assertThat(submissionDao.getCancelledSubmissions()).hasSize(1);
    assertThat(submissionDao.getFinishedSubmissions()).hasSize(1);
  }

  private OgelSubmission getScheduled(OgelSubmission.Stage stage) {
    OgelSubmission sub = new OgelSubmission("userId", "ogelType");
    sub.setScheduledMode();
    sub.setStage(stage);
    return sub;
  }
}

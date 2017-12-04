package uk.gov.bis.lite.permissions.dao;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.permissions.TestPermissionsApp;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.model.OgelSubmission.FailReason;
import uk.gov.bis.lite.permissions.model.OgelSubmission.Mode;
import uk.gov.bis.lite.permissions.model.OgelSubmission.Stage;
import uk.gov.bis.lite.permissions.model.OgelSubmission.Status;

/**
 * Integration test for OgelSubmissionDao
 */
public class SubmissionDaoTest {

  private static OgelSubmissionDao submissionDao;

  @ClassRule
  public static final DropwizardAppRule<PermissionsAppConfig> APP_RULE = new DropwizardAppRule<>(TestPermissionsApp.class,
      ResourceHelpers.resourceFilePath("test-config.yaml"));

  @BeforeClass
  public static void before() {
    Flyway flyway = new Flyway();
    DataSourceFactory dsf = APP_RULE.getConfiguration().getDataSourceFactory();
    flyway.setDataSource(dsf.getUrl(), dsf.getUser(), dsf.getPassword());
    flyway.migrate();
    submissionDao = InjectorLookup.getInjector(APP_RULE.getApplication()).get().getInstance(OgelSubmissionDao.class);
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

    OgelSubmission sub = new OgelSubmission("userId", "ogelType");
    sub.setJson("{}");
    sub.setSubmissionRef("OGEL1");
    sub.setCustomerRef("SAR1");
    sub.setSiteRef("SAR1_SITE1");
    sub.setUserId("123456");
    sub.setOgelType("OGL1");
    sub.setStatus(Status.ACTIVE);
    sub.setSpireRef("SPIRE1");
    sub.setMode(Mode.SCHEDULED);
    sub.setStage(Stage.CREATED);
    sub.setLiteJwtUser(new LiteJwtUser("123456", "test@test.com", "Mr Test"));
    sub.setFailReason(FailReason.ENDPOINT_ERROR);

    int subId = submissionDao.create(sub);
    OgelSubmission newSub = submissionDao.findBySubmissionId(subId);

    assertThat(sub.getJson()).isEqualTo(newSub.getJson());
    assertThat(sub.getSubmissionRef()).isEqualTo(newSub.getSubmissionRef());
    assertThat(sub.getCustomerRef()).isEqualTo(newSub.getCustomerRef());
    assertThat(sub.getSiteRef()).isEqualTo(newSub.getSiteRef());
    assertThat(sub.getUserId()).isEqualTo(newSub.getUserId());
    assertThat(sub.getOgelType()).isEqualTo(newSub.getOgelType());
    assertThat(sub.getStatus()).isEqualTo(newSub.getStatus());
    assertThat(sub.getSpireRef()).isEqualTo(newSub.getSpireRef());
    assertThat(sub.getMode()).isEqualTo(newSub.getMode());
    assertThat(sub.getStage()).isEqualTo(newSub.getStage());
    assertThat(sub.getLiteJwtUser().getUserId()).isEqualTo(newSub.getLiteJwtUser().getUserId());
    assertThat(sub.getLiteJwtUser().getEmail()).isEqualTo(newSub.getLiteJwtUser().getEmail());
    assertThat(sub.getLiteJwtUser().getFullName()).isEqualTo(newSub.getLiteJwtUser().getFullName());
    assertThat(sub.getLiteJwtUser().getName()).isEqualTo(newSub.getLiteJwtUser().getName());
  }

  private OgelSubmission getScheduled(Stage stage) {
    OgelSubmission sub = new OgelSubmission("userId", "ogelType");
    sub.setScheduledMode();
    sub.setStage(stage);
    return sub;
  }
}

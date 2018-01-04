package uk.gov.bis.lite.permissions.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_5;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.common.paas.db.SchemaAwareDataSourceFactory;
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

  private static EmbeddedPostgres postgres;

  private static OgelSubmissionDao submissionDao;

  private static DropwizardAppRule<PermissionsAppConfig> APP_RULE;

  private static Flyway flyway;

  @BeforeClass
  public static void beforeClass() throws Exception {
    postgres = new EmbeddedPostgres(V9_5);
    postgres.start("localhost", 5432, "dbName", "postgres", "password");

    APP_RULE = new DropwizardAppRule<>(TestPermissionsApp.class, "test-config.yaml");
    APP_RULE.getTestSupport().before();

    submissionDao = InjectorLookup.getInjector(APP_RULE.getApplication()).get().getInstance(OgelSubmissionDao.class);
    SchemaAwareDataSourceFactory dataSourceFactory = APP_RULE.getConfiguration().getDataSourceFactory();
    flyway = new Flyway();
    flyway.setDataSource(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword());
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
    sub.setLiteJwtUser(new LiteJwtUser()
        .setUserId("123456")
        .setEmail("test@test.com")
        .setFullName("Mr Test"));
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

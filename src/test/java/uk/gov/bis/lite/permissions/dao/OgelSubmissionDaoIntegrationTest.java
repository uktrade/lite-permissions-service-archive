package uk.gov.bis.lite.permissions.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_5;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.util.Duration;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.common.paas.db.SchemaAwareDataSourceFactory;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.model.OgelSubmission.FailReason;
import uk.gov.bis.lite.permissions.model.OgelSubmission.Mode;
import uk.gov.bis.lite.permissions.model.OgelSubmission.Stage;
import uk.gov.bis.lite.permissions.model.OgelSubmission.Status;

/**
 * Integration test for OgelSubmissionDao
 */
public class OgelSubmissionDaoIntegrationTest {
  private static EmbeddedPostgres postgres;

  private Flyway flyway;
  private OgelSubmissionDao submissionDao;

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
    // Build DataSourceFactory like Dropwizard would
    SchemaAwareDataSourceFactory dsf = new SchemaAwareDataSourceFactory();
    dsf.setDriverClass("org.postgresql.Driver");
    dsf.setUrl("jdbc:postgresql://localhost:5432/postgres?currentSchema=permissionsvc");
    dsf.setUser("postgres");
    dsf.setPassword("password");
    dsf.setProperties(ImmutableMap.of("charSet", "UTF-8"));
    dsf.setMaxWaitForConnection(Duration.seconds(30));
    dsf.setValidationQuery("SELECT 1");
    dsf.setInitialSize(1);
    dsf.setMinSize(1);
    dsf.setMaxSize(1);
    dsf.setCheckConnectionWhileIdle(false);
    dsf.setEvictionInterval(Duration.seconds(10));
    dsf.setMinIdleTime(Duration.minutes(1));
    submissionDao = new OgelSubmissionDaoImpl(new DBI(dsf.build(new MetricRegistry(), "postgres")));
    flyway = new Flyway();
    flyway.setDataSource(dsf.getUrl(), dsf.getUser(), dsf.getPassword());
    flyway.migrate();
  }

  @After
  public void after() {
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

    long subId = submissionDao.create(sub);
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

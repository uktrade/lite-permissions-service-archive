package uk.gov.bis.lite.permissions.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.mocks.CallbackServiceMock;
import uk.gov.bis.lite.permissions.mocks.CustomerServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelSubmissionDaoMock;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

/**
 * Unit test for ProcessSubmissionService progressStage method
 */
public class StageProgressionTest {

  private static final String CUSTOMER_REF = "CUSTOMER_REF";
  private static final String SITE_REF = "SITE_REF";
  private static final String SPIRE_REF = "SPIRE_REF";

  private ProcessSubmissionService service;

  @Before
  public void before() {
    OgelSubmissionDao submissionDao = new OgelSubmissionDaoMock();
    CustomerService customerService = new CustomerServiceMock();
    OgelService ogelService = new OgelServiceMock();
    CallbackService callbackService = new CallbackServiceMock();
    service = new ProcessSubmissionServiceImpl(submissionDao, customerService, ogelService, callbackService, 1, 3);
  }

  @Test
  public void testStandard() throws Exception {
    OgelSubmission sub = getStagedWithRoleUpdate(Util.STAGE_CREATED);
    sub.setStage(service.progressStage(sub));
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_CUSTOMER);

    sub.setCustomerRef(CUSTOMER_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_SITE);

    sub.setSiteRef(SITE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_USER_ROLE);

    sub.setRoleUpdated(true);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_OGEL);

    sub.setSpireRef(SPIRE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_OGEL);
  }

  @Test
  public void testWithExistingCustomer() throws Exception {
    OgelSubmission sub = getStagedWithRoleUpdate(Util.STAGE_CREATED);
    sub.setCustomerRef(CUSTOMER_REF);

    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_SITE);
    sub.setSiteRef(SITE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_USER_ROLE);
    sub.setRoleUpdated(true);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_OGEL);
    sub.setSpireRef(SPIRE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_OGEL);
  }

  @Test
  public void testWithExistingCustomerAndSite() throws Exception {
    OgelSubmission sub = getStagedWithRoleUpdate(Util.STAGE_CREATED);
    sub.setCustomerRef(CUSTOMER_REF);
    sub.setSiteRef(SITE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_USER_ROLE);

    sub.setRoleUpdated(true);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_OGEL);

    sub.setSpireRef(SPIRE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_OGEL);
  }

  @Test
  public void testWithoutRoleUpdate() throws Exception {
    OgelSubmission sub = getStagedWithoutRoleUpdate(Util.STAGE_CREATED);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_CUSTOMER);

    sub.setCustomerRef(CUSTOMER_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_SITE);

    sub.setSiteRef(SITE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_OGEL);

    sub.setSpireRef(SPIRE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_OGEL);
  }

  @Test
  public void testWithExistingCustomerAndSiteWithoutRoleUpdate() throws Exception {
    OgelSubmission sub = getStagedWithoutRoleUpdate(Util.STAGE_CREATED);
    sub.setCustomerRef(CUSTOMER_REF);
    sub.setSiteRef(SITE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_OGEL);

    sub.setSpireRef(SPIRE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(Util.STAGE_OGEL);
  }

  private OgelSubmission getStagedWithoutRoleUpdate(OgelSubmission.Stage stage) {
    OgelSubmission sub = getStagedWithRoleUpdate(stage);
    sub.setRoleUpdate(false);
    return sub;
  }

  private OgelSubmission getStagedWithRoleUpdate(OgelSubmission.Stage stage) {
    OgelSubmission sub = new OgelSubmission("userId", "ogelType");
    sub.setRoleUpdate(true);
    sub.setScheduledMode();
    sub.setStage(stage);
    return sub;
  }

}

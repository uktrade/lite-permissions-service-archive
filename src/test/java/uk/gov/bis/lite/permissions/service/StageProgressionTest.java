package uk.gov.bis.lite.permissions.service;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.mocks.CallbackServiceMock;
import uk.gov.bis.lite.permissions.mocks.CustomerServiceMock;
import uk.gov.bis.lite.permissions.mocks.FailServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelSubmissionDaoMock;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

/**
 * Unit test for ProcessOgelSubmissionService progressStage method
 */
public class StageProgressionTest {

  private ProcessOgelSubmissionService service;

  @Before
  public void before() {
    OgelSubmissionDao submissionDao = new OgelSubmissionDaoMock();
    CustomerService customerService = new CustomerServiceMock();
    OgelService ogelService = new OgelServiceMock();
    CallbackService callbackService = new CallbackServiceMock();
    FailService failService = new FailServiceMock();
    service = new ProcessOgelSubmissionServiceImpl(submissionDao, customerService, ogelService, callbackService, failService);
  }

  @Test
  public void testStandard() throws Exception {
    OgelSubmission sub = getStagedWithRoleUpdate(OgelSubmission.Stage.CREATED);
    sub.setStage(service.progressStage(sub));
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.CUSTOMER);

    sub.setCustomerRef("customerRef");
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.SITE);

    sub.setSiteRef("siteRef");
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.USER_ROLE);

    sub.setRoleUpdated(true);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);

    sub.setSpireRef("spireRef");
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);
  }

  @Test
  public void testWithExistingCustomer() throws Exception {
    OgelSubmission sub = getStagedWithRoleUpdate(OgelSubmission.Stage.CREATED);
    sub.setCustomerRef("customerRef");

    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.SITE);
    sub.setSiteRef("siteRef");
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.USER_ROLE);
    sub.setRoleUpdated(true);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);
    sub.setSpireRef("spireRef");
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);
  }

  @Test
  public void testWithExistingCustomerAndSite() throws Exception {
    OgelSubmission sub = getStagedWithRoleUpdate(OgelSubmission.Stage.CREATED);
    sub.setCustomerRef("customerRef");
    sub.setSiteRef("siteRef");
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.USER_ROLE);

    sub.setRoleUpdated(true);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);

    sub.setSpireRef("spireRef");
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);
  }

  @Test
  public void testWithoutRoleUpdate() throws Exception {
    OgelSubmission sub = getStagedWithoutRoleUpdate(OgelSubmission.Stage.CREATED);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.CUSTOMER);

    sub.setCustomerRef("customerRef");
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.SITE);

    sub.setSiteRef("siteRef");
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);

    sub.setSpireRef("spireRef");
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);
  }

  @Test
  public void testWithExistingCustomerAndSiteWithoutRoleUpdate() throws Exception {
    OgelSubmission sub = getStagedWithoutRoleUpdate(OgelSubmission.Stage.CREATED);
    sub.setCustomerRef("customerRef");
    sub.setSiteRef("siteRef");
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);

    sub.setSpireRef("spireRef");
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);
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

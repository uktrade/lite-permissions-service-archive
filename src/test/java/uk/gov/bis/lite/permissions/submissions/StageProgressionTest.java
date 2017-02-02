package uk.gov.bis.lite.permissions.submissions;


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
import uk.gov.bis.lite.permissions.service.CallbackService;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.FailService;
import uk.gov.bis.lite.permissions.service.OgelService;
import uk.gov.bis.lite.permissions.service.ProcessOgelSubmissionService;
import uk.gov.bis.lite.permissions.service.ProcessOgelSubmissionServiceImpl;

public class StageProgressionTest {

  private ProcessOgelSubmissionService service;
  private String CUSTOMER_REF = "CUSTOMER_REF";
  private String SITE_REF = "SITE_REF";

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
  public void testFromCreatedWithRoleUpdate() throws Exception {
    OgelSubmission sub = getStagedWithRoleUpdate(OgelSubmission.Stage.CREATED);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.CUSTOMER);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.SITE);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.USER_ROLE);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);
  }

  @Test
  public void testFromCreatedWithoutRoleUpdate() throws Exception {
    OgelSubmission sub = getStagedWithoutRoleUpdate(OgelSubmission.Stage.CREATED);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.CUSTOMER);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.SITE);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.OGEL);
  }

  @Test
  public void testFromCreatedWithExistingCustomer() throws Exception {
    OgelSubmission sub = getStagedWithRoleUpdate(OgelSubmission.Stage.CREATED);
    sub.setCustomerRef(CUSTOMER_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.SITE);
  }

  @Test
  public void testFromCreatedWithExistingCustomerExistingSite() throws Exception {
    OgelSubmission sub = getStagedWithRoleUpdate(OgelSubmission.Stage.CREATED);
    sub.setCustomerRef(CUSTOMER_REF);
    sub.setSiteRef(SITE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OgelSubmission.Stage.USER_ROLE);
  }

  @Test
  public void testFromCreatedWithExistingCustomerExistingSiteWithoutRoleUpdate() throws Exception {
    OgelSubmission sub = getStagedWithoutRoleUpdate(OgelSubmission.Stage.CREATED);
    sub.setCustomerRef(CUSTOMER_REF);
    sub.setSiteRef(SITE_REF);
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

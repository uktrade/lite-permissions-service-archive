package uk.gov.bis.lite.permissions.service;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
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

  private ProcessSubmissionService service;

  private OgelSubmission.Stage CREATED = OgelSubmission.Stage.CREATED;
  private OgelSubmission.Stage CUSTOMER = OgelSubmission.Stage.CUSTOMER;
  private OgelSubmission.Stage SITE = OgelSubmission.Stage.SITE;
  private OgelSubmission.Stage USER_ROLE = OgelSubmission.Stage.USER_ROLE;
  private OgelSubmission.Stage OGEL = OgelSubmission.Stage.OGEL;

  private String CUSTOMER_REF = "CUSTOMER_REF";
  private String SITE_REF = "SITE_REF";
  private String SPIRE_REF = "SPIRE_REF";

  @Before
  public void before() {
    OgelSubmissionDao submissionDao = new OgelSubmissionDaoMock();
    CustomerService customerService = new CustomerServiceMock();
    OgelService ogelService = new OgelServiceMock();
    CallbackService callbackService = new CallbackServiceMock();
    service = new ProcessSubmissionServiceImpl(submissionDao, customerService, ogelService, callbackService, 1);
  }

  @Test
  public void testStandard() throws Exception {
    OgelSubmission sub = getStagedWithRoleUpdate(CREATED);
    sub.setStage(service.progressStage(sub));
    assertThat(sub.getStage()).isEqualTo(CUSTOMER);

    sub.setCustomerRef(CUSTOMER_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(SITE);

    sub.setSiteRef(SITE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(USER_ROLE);

    sub.setRoleUpdated(true);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OGEL);

    sub.setSpireRef(SPIRE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OGEL);
  }

  @Test
  public void testWithExistingCustomer() throws Exception {
    OgelSubmission sub = getStagedWithRoleUpdate(CREATED);
    sub.setCustomerRef(CUSTOMER_REF);

    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(SITE);
    sub.setSiteRef(SITE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(USER_ROLE);
    sub.setRoleUpdated(true);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OGEL);
    sub.setSpireRef(SPIRE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OGEL);
  }

  @Test
  public void testWithExistingCustomerAndSite() throws Exception {
    OgelSubmission sub = getStagedWithRoleUpdate(CREATED);
    sub.setCustomerRef(CUSTOMER_REF);
    sub.setSiteRef(SITE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(USER_ROLE);

    sub.setRoleUpdated(true);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OGEL);

    sub.setSpireRef(SPIRE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OGEL);
  }

  @Test
  public void testWithoutRoleUpdate() throws Exception {
    OgelSubmission sub = getStagedWithoutRoleUpdate(CREATED);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(CUSTOMER);

    sub.setCustomerRef(CUSTOMER_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(SITE);

    sub.setSiteRef(SITE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OGEL);

    sub.setSpireRef(SPIRE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OGEL);
  }

  @Test
  public void testWithExistingCustomerAndSiteWithoutRoleUpdate() throws Exception {
    OgelSubmission sub = getStagedWithoutRoleUpdate(CREATED);
    sub.setCustomerRef(CUSTOMER_REF);
    sub.setSiteRef("siteRef");
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OGEL);

    sub.setSpireRef(SPIRE_REF);
    service.progressStage(sub);
    assertThat(sub.getStage()).isEqualTo(OGEL);
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

package uk.gov.bis.lite.permissions.service;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.mocks.OgelSubmissionDaoMock;
import uk.gov.bis.lite.permissions.mocks.ProcessSubmissionServiceMock;

import java.io.IOException;

public class RegisterParamValidationTest {

  private RegisterService service;
  private ObjectMapper mapper;

  @Before
  public void before() {
    OgelSubmissionDao submissionDao = new OgelSubmissionDaoMock();
    ProcessSubmissionServiceMock processOgelSubmissionServiceMock = new ProcessSubmissionServiceMock();
    service = new RegisterServiceImpl(submissionDao, null, processOgelSubmissionServiceMock);
    mapper = new ObjectMapper();
  }

  @Test
  public void testRegisterParamValidation() throws Exception {
    String PATH = "fixture/registerParam/";

    // Valid
    assertThat(service.isRegisterParamValid(getRegisterParam(fixture(PATH + "valid.json")))).isEqualTo(true);
    assertThat(service.isRegisterParamValid(getRegisterParam(fixture(PATH + "validNewSiteWithSiteName1.json")))).isEqualTo(true);
    assertThat(service.isRegisterParamValid(getRegisterParam(fixture(PATH + "validNewSiteWithSiteName2.json")))).isEqualTo(true);

    // Invalid
    assertThat(service.isRegisterParamValid(getRegisterParam(fixture(PATH + "invalidNewCustomerExistingSite.json")))).isEqualTo(false);
    assertThat(service.isRegisterParamValid(getRegisterParam(fixture(PATH + "invalidMandatoryFieldMissing1.json")))).isEqualTo(false);
    assertThat(service.isRegisterParamValid(getRegisterParam(fixture(PATH + "invalidMandatoryFieldMissing2.json")))).isEqualTo(false);
    assertThat(service.isRegisterParamValid(getRegisterParam(fixture(PATH + "invalidNoCustomer.json")))).isEqualTo(false);
    assertThat(service.isRegisterParamValid(getRegisterParam(fixture(PATH + "invalidNoSite.json")))).isEqualTo(false);
    assertThat(service.isRegisterParamValid(getRegisterParam(fixture(PATH + "invalidNewSite1.json")))).isEqualTo(false);
    assertThat(service.isRegisterParamValid(getRegisterParam(fixture(PATH + "invalidNewSite2.json")))).isEqualTo(false);
    assertThat(service.isRegisterParamValid(getRegisterParam(fixture(PATH + "invalidNewSite3.json")))).isEqualTo(false);
    assertThat(service.isRegisterParamValid(getRegisterParam(fixture(PATH + "invalidNewSiteWithoutSiteName1.json")))).isEqualTo(false);
    assertThat(service.isRegisterParamValid(getRegisterParam(fixture(PATH + "invalidNewSiteWithoutSiteName2.json")))).isEqualTo(false);
  }

  private RegisterParam getRegisterParam(String json) {
    RegisterParam param = null;
    try {
      param = mapper.readValue(json, RegisterParam.class);
    } catch (IOException e) {
      // ignore
    }
    return param;
  }
}

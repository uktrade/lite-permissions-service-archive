package uk.gov.bis.lite.permissions.pact;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for PermissionService consumer pact tests for CustomerService provider
 */
class CustomerBasePactTest {

  private ObjectMapper mapper = new ObjectMapper();

  final static String CONSUMER = "lite-permissions-service";
  final static String PROVIDER = "lite-customer-service";

  final static String FIXTURE_REGISTER_PARAM_NEW = "fixture/pact/registerParamNew.json";
  final static String FIXTURE_REGISTER_PARAM_EXISTING = "fixture/pact/registerParamExisting.json";

  Map<String, String> headers() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    return headers;
  }

  RegisterParam getRegisterParam(String fixture) {
    RegisterParam param = null;
    try {
      param = mapper.readValue(fixture, RegisterParam.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return param;
  }

  /**
   * Returns a new OgelSubmission from RegisterParam
   */
  OgelSubmission getOgelSubmission(RegisterParam param) {
    OgelSubmission sub = new OgelSubmission(param.getUserId(), param.getOgelType());
    sub.setCustomerRef(param.getExistingCustomer());
    sub.setSiteRef(param.getExistingSite());
    sub.setSubmissionRef(RandomStringUtils.randomAlphabetic(6));
    sub.setRoleUpdate(param.roleUpdateRequired());
    sub.setCalledBack(false);
    try {
      sub.setJson(mapper.writeValueAsString(param));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    if (param.getAdminApproval() != null) {
      String adminUserId = param.getAdminApproval().getAdminUserId();
      if (!StringUtils.isBlank(adminUserId)) {
        sub.setAdminUserId(adminUserId);
      }
    }
    return sub;
  }
}

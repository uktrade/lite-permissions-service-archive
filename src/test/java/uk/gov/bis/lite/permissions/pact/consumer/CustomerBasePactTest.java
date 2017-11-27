package uk.gov.bis.lite.permissions.pact.consumer;

import static io.dropwizard.testing.FixtureHelpers.fixture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Base class for PermissionService consumer pact tests for CustomerService provider
 */
class CustomerBasePactTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  static final String CONSUMER = "lite-permissions-service";
  static final String PROVIDER = "lite-customer-service";

  static final String FIXTURE_REGISTER_PARAM_EXISTING = "fixture/pact/registerParamExisting.json";
  static final String FIXTURE_REGISTER_PARAM_NEW = "fixture/pact/registerParamNew.json";

  Map<String, String> headers() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    return headers;
  }

  RegisterParam getRegisterParam(String fixture) {
    RegisterParam param = null;
    try {
      param = MAPPER.readValue(fixture, RegisterParam.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return param;
  }

  OgelSubmission ogelSubmission() {
    return getOgelSubmission(getRegisterParam(fixture(FIXTURE_REGISTER_PARAM_NEW)));
  }

  /**
   * Returns a new OgelSubmission from RegisterParam
   */
  OgelSubmission getOgelSubmission(RegisterParam param) {
    OgelSubmission sub = new OgelSubmission(param.getUserId(), param.getOgelType());
    sub.setCustomerRef(param.getExistingCustomer());
    sub.setSiteRef(param.getExistingSite());
    sub.setSubmissionRef(UUID.randomUUID().toString().substring(6));
    sub.setRoleUpdate(param.roleUpdateRequired());
    sub.setCalledBack(false);
    try {
      sub.setJson(MAPPER.writeValueAsString(param));
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

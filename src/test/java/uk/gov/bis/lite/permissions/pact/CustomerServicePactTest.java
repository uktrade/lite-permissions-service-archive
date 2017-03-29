package uk.gov.bis.lite.permissions.pact;


import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.CustomerService;
import uk.gov.bis.lite.permissions.service.CustomerServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.ClientBuilder;

/**
 * CustomerServicePactTest - creates Pact file: lie-permissions-service-lite-customer-service.json
 */
public class CustomerServicePactTest {

  private CustomerService customerService;
  private ObjectMapper mapper = new ObjectMapper();

  private static final String CUSTOMER_ID = "CUSTOMER_ID";
  private static final String ADMIN_USER_ID = "ADMIN_USER_ID";
  private static final String COMPANY_NUMBER_SUCCESS = "COMPANY_NUMBER_SUCCESS";
  private static final String COMPANY_NUMBER_FAIL = "COMPANY_NUMBER_FAIL";
  private static final String USER_ID_SUCCESS = "USER_ID_SUCCESS";
  private static final String SITE_REF_SUCCESS = "SITE_REF_SUCCESS";
  private static final String USER_ID_FAIL = "USER_ID_FAIL";
  private static final String SITE_REF_FAIL = "SITE_REF_FAIL";

  // Customer Fixtures
  // Currently cannot use DSL because it fails to match properly for userId (with and without)
  private static String mockCustomerParam = fixture("fixture/pact/mockCustomerParam.json");
  private static String mockCustomerParamNoUserId = fixture("fixture/pact/mockCustomerParamNoUserId.json");

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("lite-customer-service", this);

  @Before
  public void before() {
    customerService = new CustomerServiceImpl(ClientBuilder.newClient(), mockProvider.getConfig().url());
  }

  @Pact(provider = "lite-customer-service", consumer = "lite-permissions-service")
  public PactFragment createFragment(PactDslWithProvider builder) {

    return builder
        .uponReceiving("Create Customer Success")
          .path("/create-customer")
          .headers(headers())
          .method("POST")
          .body(mockCustomerParam)
          .willRespondWith()
            .headers(headers())
            .status(200)
            .body(customerViewDsl())
        .uponReceiving("Create Customer Fail (no userId)")
          .path("/create-customer")
          .headers(headers())
          .method("POST")
          .body(mockCustomerParamNoUserId)
          .willRespondWith()
            .status(400)
        .uponReceiving("Create Site Success")
          .path("/customer-sites/" + CUSTOMER_ID)
          .headers(headers())
          .method("POST")
          .query("userId=userId")
          .body(siteParamDsl())
          .willRespondWith()
            .headers(headers())
            .status(200)
            .body(mockSiteViewDsl())
        .uponReceiving("Create Site Fail (no userId)")
          .path("/customer-sites/" + CUSTOMER_ID)
          .headers(headers())
          .method("POST")
          .query("userId=")
          .body(siteParamDsl())
          .willRespondWith()
            .status(400)
        .uponReceiving("Customer by CompanyNumber Success")
          .path("/search-customers/registered-number/" + COMPANY_NUMBER_SUCCESS)
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers())
            .body(customerViewDsl())
        .uponReceiving("Customer by CompanyNumber Fail")
          .path("/search-customers/registered-number/" + COMPANY_NUMBER_FAIL)
          .method("GET")
          .willRespondWith()
            .status(404)
        .uponReceiving("Update UserRole Success")
          .path("/user-roles/user/" + USER_ID_SUCCESS + "/site/" + SITE_REF_SUCCESS)
          .headers(headers())
          .method("POST")
          .body(userRoleParamDsl())
          .willRespondWith()
            .status(200)
        .uponReceiving("Update UserRole Fail")
          .path("/user-roles/user/" + USER_ID_FAIL + "/site/" + SITE_REF_FAIL)
          .headers(headers())
          .method("POST")
          .body(userRoleParamDsl())
          .willRespondWith()
            .status(400)
        .toFragment();
  }

  @Test
  @PactVerification("lite-customer-service")
  public void testCustomerServicePact() throws Exception {

    // Create Customer Success
    OgelSubmission sub1 = getOgelSubmission(getRegisterParam(fixture("fixture/pact/mockRegisterParamNew.json")));
    assertThat(customerService.createCustomer(sub1)).isPresent();

    // Create Customer Fail (no userId)
    OgelSubmission sub2 = getOgelSubmission(getRegisterParam(fixture("fixture/pact/mockRegisterParamNew.json")));
    sub2.setUserId("");
    assertThat(customerService.createCustomer(sub2)).isNotPresent();

    // Create Site Success
    OgelSubmission sub3 = getOgelSubmission(getRegisterParam(fixture("fixture/pact/mockRegisterParamNew.json")));
    sub3.setCustomerRef(CUSTOMER_ID);
    assertThat(customerService.createSite(sub3)).isPresent();

    // Create Site Fail (no userId)
    OgelSubmission sub4 = getOgelSubmission(getRegisterParam(fixture("fixture/pact/mockRegisterParamNew.json")));
    sub4.setCustomerRef(CUSTOMER_ID);
    sub4.setUserId("");
    assertThat(customerService.createSite(sub4)).isNotPresent();

    // Customer by CompanyNumber Success
    assertThat(customerService.getCustomerIdByCompanyNumber(COMPANY_NUMBER_SUCCESS)).isPresent();

    // Customer by CompanyNumber NoFound
    assertThat(customerService.getCustomerIdByCompanyNumber(COMPANY_NUMBER_FAIL)).isNotPresent();

    // Update UserRole Success
    OgelSubmission sub5 = getOgelSubmission(getRegisterParam(fixture("fixture/pact/mockRegisterParamExisting.json")));
    sub5.setAdminUserId(ADMIN_USER_ID);
    sub5.setSiteRef(SITE_REF_SUCCESS);
    sub5.setUserId(USER_ID_SUCCESS);
    assertThat(customerService.updateUserRole(sub5)).isTrue();

    // Update UserRole Fail
    OgelSubmission sub6 = getOgelSubmission(getRegisterParam(fixture("fixture/pact/mockRegisterParamExisting.json")));
    sub6.setAdminUserId(ADMIN_USER_ID);
    sub6.setSiteRef(SITE_REF_FAIL);
    sub6.setUserId(USER_ID_FAIL);
    assertThat(customerService.updateUserRole(sub6)).isFalse();

  }

  private PactDslJsonBody customerParamNoUserIdDsl() {
    return new PactDslJsonBody()
        .stringMatcher("userId", "")
        .stringType("customerName")
        .stringType("customerType")
        .stringType("website")
        .stringType("companiesHouseNumber")
        .booleanType("companiesHouseValidated")
        .stringType("eoriNumber")
        .booleanType("eoriValidated")
        .object("addressParam")
        .stringType("line1")
        .stringType("line2")
        .stringType("town")
        .stringType("county")
        .stringType("postcode")
        .stringType("country")
        .closeObject()
        .asBody();
  }

  private PactDslJsonBody customerParamDsl() {
    return new PactDslJsonBody()
        .stringType("userId")
        .stringType("customerName")
        .stringType("customerType")
        .stringType("website")
        .stringType("companiesHouseNumber")
        .booleanType("companiesHouseValidated")
        .stringType("eoriNumber")
        .booleanType("eoriValidated")
        .object("addressParam")
        .stringType("line1")
        .stringType("line2")
        .stringType("town")
        .stringType("county")
        .stringType("postcode")
        .stringType("country")
        .closeObject()
        .asBody();
  }

  private PactDslJsonBody siteParamDsl() {
    return new PactDslJsonBody()
        .stringType("siteName")
        .object("addressParam")
        .stringType("line1")
        .stringType("line2")
        .stringType("town")
        .stringType("county")
        .stringType("postcode")
        .stringType("country")
        .closeObject()
        .asBody();
  }

  private PactDslJsonBody userRoleParamDsl() {
    // Requires 'ADMIN' value because API UserRoleParam param contains enum RoleType [ADMIN, SUBMITTER, PREPARER]
    return new PactDslJsonBody()
        .stringType("adminUserId")
        .stringType("roleType", "ADMIN")
        .asBody();
  }

  private PactDslJsonBody customerViewDsl() {
    return new PactDslJsonBody()
        .stringType("customerId")
        .stringType("companyName")
        .stringType("companyNumber")
        .stringType("shortName")
        .stringType("organisationType")
        .stringType("registrationStatus")
        .stringType("registeredAddress")
        .stringType("applicantType")
        .stringType("countryOfOriginCode")
        .array("websites").closeArray()
        .asBody();
  }

  private PactDslJsonBody mockSiteViewDsl() {
    return new PactDslJsonBody()
        .stringType("siteId")
        .stringType("customerId")
        .stringType("siteName")
        .object("address")
        .stringType("plainText")
        .stringType("country")
        .closeObject()
        .asBody();
  }

  private Map<String, String> headers() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    return headers;
  }

  private RegisterParam getRegisterParam(String fixture) {
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
  private OgelSubmission getOgelSubmission(RegisterParam param) {
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

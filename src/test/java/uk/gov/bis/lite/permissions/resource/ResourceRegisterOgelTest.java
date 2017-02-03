package uk.gov.bis.lite.permissions.resource;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.api.RegisterOgelResponse;
import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.mocks.RegisterServiceMock;
import uk.gov.bis.lite.permissions.mocks.OgelSubmissionServiceMock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ResourceRegisterOgelTest {

  private int OK = Response.Status.OK.getStatusCode();
  private int NOT_OK = Response.Status.BAD_REQUEST.getStatusCode();

  private static String MOCK_USER_ID = "ID1";
  private static String MOCK_OGEL_TYPE = "TYPE1";
  private static String MOCK_SAR_REF = "SAR1";
  private static String MOCK_SITE_REF = "SITE1";
  private static String MOCK_SUBMISSION_REF = "SUB1";
  private static RegisterServiceMock mockRegisterService = new RegisterServiceMock(MOCK_SUBMISSION_REF);
  private static OgelSubmissionServiceMock mockSubmissionService = new OgelSubmissionServiceMock();

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new RegisterOgelResource(mockRegisterService, mockSubmissionService)).build();

  @Test
  public void invalidRegisterOgel() {
    Response response = request("/register-ogel", MediaType.APPLICATION_JSON)
        .post(Entity.entity(new RegisterParam(), MediaType.APPLICATION_JSON));
    assertThat(status(response)).isEqualTo(NOT_OK);
  }

  @Test
  public void alreadyExistsRegisterOgel() {
    mockSubmissionService.setSubmissionCurrentlyExists(true); // Update mockSubmissionService first
    Response response = request("/register-ogel", MediaType.APPLICATION_JSON)
        .post(Entity.entity(getValidRegisterOgel(), MediaType.APPLICATION_JSON));
    assertThat(status(response)).isEqualTo(NOT_OK);
  }

  @Test
  public void validRegisterOgel() {
    mockSubmissionService.setSubmissionCurrentlyExists(false);  // Update mockSubmissionService first
    Response response = request("/register-ogel", MediaType.APPLICATION_JSON)
        .post(Entity.entity(getValidRegisterOgel(), MediaType.APPLICATION_JSON));
    assertThat(status(response)).isEqualTo(OK);
    assertThat(getResponseRequestId(response)).isEqualTo(MOCK_SUBMISSION_REF);
  }

  /**
   * private methods
   */
  private Invocation.Builder request(String url, String mediaType) {
    return target(url).request(mediaType);
  }

  private WebTarget target(String url) {
    return resources.client().target(url);
  }

  private int status(Response response) {
    return response.getStatus();
  }

  private RegisterParam getValidRegisterOgel() {
    RegisterParam reg = new RegisterParam();
    reg.setUserId(MOCK_USER_ID);
    reg.setOgelType(MOCK_OGEL_TYPE);
    reg.setExistingCustomer(MOCK_SAR_REF);
    reg.setExistingSite(MOCK_SITE_REF);
    return reg;
  }

  private String getResponseRequestId(Response response) {
    return response.readEntity(RegisterOgelResponse.class).getRequestId();
  }
}

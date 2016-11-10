package uk.gov.bis.lite.permissions.resource;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.mocks.RegisterServiceMock;
import uk.gov.bis.lite.permissions.mocks.SubmissionServiceMock;
import uk.gov.bis.lite.permissions.model.register.RegisterOgel;
import uk.gov.bis.lite.permissions.model.register.RegisterOgelResponse;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ResourceRegisterOgelTest {

  private int OK = Response.Status.OK.getStatusCode();
  private int NOT_OK = Response.Status.BAD_REQUEST.getStatusCode();
  private static ObjectMapper mapper = new ObjectMapper();

  private static String MOCK_USER_ID = "ID1";
  private static String MOCK_OGEL_TYPE = "TYPE1";
  private static String MOCK_SAR_REF = "SAR1";
  private static String MOCK_SITE_REF = "SITE1";
  private static String MOCK_SUBMISSION_REF = "SUB1";
  private static RegisterServiceMock mockRegisterService = new RegisterServiceMock(MOCK_SUBMISSION_REF);
  private static SubmissionServiceMock mockSubmissionService = new SubmissionServiceMock();

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new RegisterOgelResource(mockRegisterService, mockSubmissionService)).build();

  @Test
  public void invalidRegisterOgel() {
    Response response = request("/register-ogel", MediaType.APPLICATION_JSON)
        .post(Entity.entity(getRegisterOgelJson(new RegisterOgel()), MediaType.APPLICATION_JSON));
    assertThat(status(response)).isEqualTo(NOT_OK);
  }

  @Test
  public void alreadyExistsRegisterOgel() {

    mockSubmissionService.setSubmissionCurrentlyExists(true); // Update mockSubmissionService first

    Response response = request("/register-ogel", MediaType.APPLICATION_JSON)
        .post(Entity.entity(getRegisterOgelJson(getValidRegisterOgel()), MediaType.APPLICATION_JSON));

    assertThat(status(response)).isEqualTo(NOT_OK);
  }

  @Test
  public void validRegisterOgel() {

    mockSubmissionService.setSubmissionCurrentlyExists(false);  // Update mockSubmissionService first

    Response response = request("/register-ogel", MediaType.APPLICATION_JSON)
        .post(Entity.entity(getRegisterOgelJson(getValidRegisterOgel()), MediaType.APPLICATION_JSON));

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

  private String getRegisterOgelJson(RegisterOgel ogel) {
    String json = "";
    try {
      json = mapper.writeValueAsString(ogel);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return json;
  }

  private RegisterOgel getValidRegisterOgel() {
    RegisterOgel reg = new RegisterOgel();
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

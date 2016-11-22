package uk.gov.bis.lite.permissions.resource;


import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.mocks.RegistrationsServiceMock;

import java.util.List;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class ResourceOgelRegistrationTest {

  private int OK = Response.Status.OK.getStatusCode();
  private int NOT_FOUND = Response.Status.NOT_FOUND.getStatusCode();

  private static int MOCK_REGISTRATIONS_NUMBER = 3;
  private static String MOCK_REGISTRATION_TAG = "SPIRE";
  private static RegistrationsServiceMock mockRegistrationsService = new RegistrationsServiceMock(MOCK_REGISTRATION_TAG, MOCK_REGISTRATIONS_NUMBER);

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new OgelRegistrationResource(mockRegistrationsService)).build();

  @Test
  public void viewOgelRegistrations() {
    Response response = request("/ogel-registrations/user/1").get();
    assertThat(status(response)).isEqualTo(OK);
    assertThat(getOgelRegistrationsResponse(response).size()).isEqualTo(MOCK_REGISTRATIONS_NUMBER);
  }

  @Test
  public void viewOgelRegistrationsWithParam1() {
    Response response = request("/ogel-registrations/user/1?registrationReference=" + MOCK_REGISTRATION_TAG + "1").get();
    assertThat(status(response)).isEqualTo(OK);
    assertThat(getOgelRegistrationsResponse(response).size()).isEqualTo(1);
  }

  @Test
  public void viewOgelRegistrationsWithParam2() {
    Response response = request("/ogel-registrations/user/1?registrationReference=NOT_THERE").get();
    assertThat(status(response)).isEqualTo(NOT_FOUND);
  }

  @Test
  public void viewOgelRegistrationsNoResults() {
    mockRegistrationsService.setNoResults(true); // Update mockRegistrationsService first
    Response response = request("/ogel-registrations/user/1").get();
    assertThat(status(response)).isEqualTo(NOT_FOUND);
  }

  /**
   * Private Methods
   */
  private List<OgelRegistrationView> getOgelRegistrationsResponse(Response response) {
    return (List<OgelRegistrationView>) response.readEntity(List.class);
  }

  private Invocation.Builder request(String url) {
    return target(url).request();
  }

  private WebTarget target(String url) {
    return resources.client().target(url);
  }

  private int status(Response response) {
    return response.getStatus();
  }
}
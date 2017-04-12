package uk.gov.bis.lite.permissions.mocks.pact;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.RegistrationsService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Singleton
public class RegistrationsServiceMock implements RegistrationsService {

  private boolean hasRegistrations = true;

  public List<OgelRegistrationView> getRegistrations(String userId, String registrationReference) {
    if (hasRegistrations) {
      return buildRegistrations();
    } else {
      return Collections.emptyList();
    }
  }

  public List<OgelRegistrationView> getRegistrations(String userId) {
    if (hasRegistrations) {
      return buildRegistrations();
    } else {
      return Collections.emptyList();
    }
  }

  private List<OgelRegistrationView> buildRegistrations() {
    OgelRegistrationView ogelRegistrationView = new OgelRegistrationView();
    ogelRegistrationView.setRegistrationReference("REG_REF");
    ogelRegistrationView.setCustomerId("CUST1");
    ogelRegistrationView.setStatus(OgelRegistrationView.Status.UNKNOWN);
    ogelRegistrationView.setSiteId("SITE1");
    ogelRegistrationView.setOgelType("OGEL_TYPE");
    ogelRegistrationView.setRegistrationDate("DATE");
    return Arrays.asList(ogelRegistrationView);
  }

  public void setHasRegistrations(boolean hasRegistrations) {
    this.hasRegistrations = hasRegistrations;
  }
}

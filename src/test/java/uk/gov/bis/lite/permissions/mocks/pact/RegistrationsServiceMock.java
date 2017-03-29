package uk.gov.bis.lite.permissions.mocks.pact;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.RegistrationsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class RegistrationsServiceMock implements RegistrationsService {


  public List<OgelRegistrationView> getRegistrations(String userId, String registrationReference) {
    return buildRegistrations();
  }

  public List<OgelRegistrationView> getRegistrations(String userId) {
    return buildRegistrations();
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
}

package uk.gov.bis.lite.permissions.mocks.pact;

import static java.util.Collections.emptyList;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.RegistrationsService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Singleton
public class RegistrationsServiceMock implements RegistrationsService {

  private boolean noResults = false;
  private boolean userNotFound = false;

  public void resetState() {
    setNoResults(false);
    setUserNotFound(false);
  }

  public Optional<List<OgelRegistrationView>> getRegistrations(String userId, String registrationReference) {
    if (userNotFound) {
      return Optional.empty();
    } else if (noResults) {
      return Optional.of(emptyList());
    } else {
      return Optional.of(buildRegistrations());
    }
  }

  public Optional<List<OgelRegistrationView>> getRegistrations(String userId) {
    if (userNotFound) {
      return Optional.empty();
    } else if (noResults) {
      return Optional.of(emptyList());
    } else {
      return Optional.of(buildRegistrations());
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

  public void setNoResults(boolean noResults) {
    this.noResults = noResults;
  }

  public void setUserNotFound(boolean userNotFound) {
    this.userNotFound = userNotFound;
  }
}

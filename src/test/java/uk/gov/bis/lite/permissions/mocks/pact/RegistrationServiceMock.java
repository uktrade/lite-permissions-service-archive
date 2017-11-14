package uk.gov.bis.lite.permissions.mocks.pact;

import static java.util.Collections.emptyList;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.RegistrationService;
import uk.gov.bis.lite.permissions.service.model.Status;
import uk.gov.bis.lite.permissions.service.model.registration.MultipleRegistrationResult;
import uk.gov.bis.lite.permissions.service.model.registration.SingleRegistrationResult;

import java.util.Arrays;
import java.util.List;

@Singleton
public class RegistrationServiceMock implements RegistrationService {

  private boolean noResults = false;
  private boolean userNotFound = false;

  public void resetState() {
    setNoResults(false);
    setUserNotFound(false);
  }

  public SingleRegistrationResult getRegistration(String userId, String registrationReference) {
    if (userNotFound) {
      return new SingleRegistrationResult(Status.USER_ID_NOT_FOUND, null);
    } else if (noResults) {
      return new SingleRegistrationResult(Status.OK, null);
    } else {
      return new SingleRegistrationResult(Status.OK, buildRegistrations().get(0));
    }
  }

  public MultipleRegistrationResult getRegistrations(String userId) {
    if (userNotFound) {
      return new MultipleRegistrationResult(Status.USER_ID_NOT_FOUND, null);
    } else if (noResults) {
      return new MultipleRegistrationResult(Status.OK, null);
    } else {
      return new MultipleRegistrationResult(Status.OK, buildRegistrations());
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

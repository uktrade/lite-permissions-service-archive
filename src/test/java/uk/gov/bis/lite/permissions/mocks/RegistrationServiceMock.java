package uk.gov.bis.lite.permissions.mocks;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.RegistrationService;
import uk.gov.bis.lite.permissions.service.model.RegistrationResult;
import uk.gov.bis.lite.permissions.service.model.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class RegistrationServiceMock implements RegistrationService {

  private boolean noResults = false;
  private boolean userNotFound = false;

  public void resetState() {
    setNoResults(false);
    setUserNotFound(false);
  }

  public RegistrationResult getRegistration(String userId, String reference) {
    if (userNotFound) {
      return new RegistrationResult(Status.USER_ID_NOT_FOUND, "Unable to find user with user id 1", null);
    } else if (noResults) {
      return new RegistrationResult(Status.REGISTRATION_NOT_FOUND, "No licence with reference REG_REF found for userId 1", null);
    } else {
      return new RegistrationResult(Status.OK, null, buildRegistrations());
    }
  }

  public RegistrationResult getRegistrations(String userId) {
    if (userNotFound) {
      return new RegistrationResult(Status.USER_ID_NOT_FOUND, "Unable to find user with user id 1", null);
    } else if (noResults) {
      return new RegistrationResult(Status.OK, null, new ArrayList<>());
    } else {
      return new RegistrationResult(Status.OK, null, buildRegistrations());
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
    return Collections.singletonList(ogelRegistrationView);
  }

  public void setNoResults(boolean noResults) {
    this.noResults = noResults;
  }

  public void setUserNotFound(boolean userNotFound) {
    this.userNotFound = userNotFound;
  }
}

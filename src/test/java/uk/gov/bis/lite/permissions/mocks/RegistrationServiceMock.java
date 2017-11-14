package uk.gov.bis.lite.permissions.mocks;

import static java.util.Collections.emptyList;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.RegistrationService;
import uk.gov.bis.lite.permissions.service.model.Status;
import uk.gov.bis.lite.permissions.service.model.registration.MultipleRegistrationResult;
import uk.gov.bis.lite.permissions.service.model.registration.SingleRegistrationResult;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class RegistrationServiceMock implements RegistrationService {

  private List<OgelRegistrationView> mockRegistrations = new ArrayList<>();
  private boolean noResults = false;
  private boolean userNotFound = false;
  private String mockRegistrationTag;

  public RegistrationServiceMock() {
    this("1234", 1);
  }

  public RegistrationServiceMock(String mockRegistrationTag, int numberOfCustomers) {
    this.mockRegistrationTag = mockRegistrationTag;
    initOgelRegistrations(numberOfCustomers);
  }

  public void resetState() {
    setNoResults(false);
    setUserNotFound(false);
  }

  private void initOgelRegistrations(int numberOfRegistrations) {
    for (int i = 1; i < numberOfRegistrations + 1; i++) {
      OgelRegistrationView view = new OgelRegistrationView();
      view.setRegistrationReference(mockRegistrationTag + i);
      view.setCustomerId("CUST" + i);
      view.setStatus(OgelRegistrationView.Status.UNKNOWN);
      view.setSiteId("SITE" + i);
      view.setOgelType("OGEL_TYPE");
      view.setRegistrationDate("DATE");
      mockRegistrations.add(view);
    }
  }

  public SingleRegistrationResult getRegistration(String userId, String registrationReference) {
    if (userNotFound) {
      return new SingleRegistrationResult(Status.USER_ID_NOT_FOUND, null);
    }
    if (noResults) {
      return new SingleRegistrationResult(Status.OK, null);
    }
    OgelRegistrationView registration = mockRegistrations
        .stream()
        .filter(or -> or.getRegistrationReference().equals(registrationReference))
        .findFirst()
        .get();
    return new SingleRegistrationResult(Status.OK, registration);
  }

  public MultipleRegistrationResult getRegistrations(String userId) {
    if (userNotFound) {
      return new MultipleRegistrationResult(Status.USER_ID_NOT_FOUND, null);
    }
    if (noResults) {
      return new MultipleRegistrationResult(Status.OK, null);
    }
    return new MultipleRegistrationResult(Status.OK, mockRegistrations);
  }

  public void setNoResults(boolean noResults) {
    this.noResults = noResults;
  }

  public void  setUserNotFound(boolean userNotFound) {
    this.userNotFound = userNotFound;
  }
}

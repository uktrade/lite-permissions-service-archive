package uk.gov.bis.lite.permissions.mocks;

import static java.util.Collections.emptyList;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.RegistrationsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class RegistrationsServiceMock implements RegistrationsService {

  private List<OgelRegistrationView> mockRegistrations = new ArrayList<>();
  private boolean noResults = false;
  private boolean userNotFound = false;
  private String mockRegistrationTag;

  public RegistrationsServiceMock() {
    this("1234", 1);
  }

  public RegistrationsServiceMock(String mockRegistrationTag, int numberOfCustomers) {
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

  public Optional<List<OgelRegistrationView>> getRegistrations(String userId, String registrationReference) {
    if (userNotFound) {
      return Optional.empty();
    }
    if (noResults) {
      return Optional.of(emptyList());
    }
    return Optional.of(mockRegistrations.stream().filter(or -> or.getRegistrationReference().equals(registrationReference)).collect(Collectors.toList()));
  }

  public Optional<List<OgelRegistrationView>> getRegistrations(String userId) {
    if (userNotFound) {
      return Optional.empty();
    }
    if (noResults) {
      return Optional.of(emptyList());
    }
    return Optional.of(mockRegistrations);
  }

  public void setNoResults(boolean noResults) {
    this.noResults = noResults;
  }

  public void  setUserNotFound(boolean userNotFound) {
    this.userNotFound = userNotFound;
  }
}

package uk.gov.bis.lite.permissions.mocks;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.RegistrationsService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class RegistrationsServiceMock implements RegistrationsService {


  private List<OgelRegistrationView> mockRegistrations = new ArrayList<>();
  private boolean noResults = false;
  private String mockRegistrationTag;

  public RegistrationsServiceMock(String mockRegistrationTag, int numberOfCustomers) {
    this.mockRegistrationTag = mockRegistrationTag;
    initOgelRegistrations(numberOfCustomers);
  }

  private void initOgelRegistrations(int numberOfRegistrations) {
    for (int i = 1; i < numberOfRegistrations + 1; i++) {
      OgelRegistrationView view = new OgelRegistrationView();
      view.setRegistrationReference(mockRegistrationTag + i);
      mockRegistrations.add(view);
    }
  }

  public List<OgelRegistrationView> getRegistrations(String userId, String registrationReference) {
    return mockRegistrations.stream().filter(or -> or.getRegistrationReference().equals(registrationReference)).collect(Collectors.toList());
  }

  public List<OgelRegistrationView> getRegistrations(String userId) {
    if (noResults) {
      return new ArrayList<>();
    }
    return mockRegistrations;
  }

  public void setNoResults(boolean noResults) {
    this.noResults = noResults;
  }
}

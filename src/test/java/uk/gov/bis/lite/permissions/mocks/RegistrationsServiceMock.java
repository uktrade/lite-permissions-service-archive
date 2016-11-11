package uk.gov.bis.lite.permissions.mocks;

import uk.gov.bis.lite.permissions.model.OgelRegistration;
import uk.gov.bis.lite.permissions.service.RegistrationsService;
import uk.gov.bis.lite.permissions.spire.model.SpireOgelRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegistrationsServiceMock implements RegistrationsService {


  private List<OgelRegistration> mockRegistrations = new ArrayList<>();
  private boolean noResults = false;
  private String mockRegistrationTag;

  public RegistrationsServiceMock(String mockRegistrationTag, int numberOfCustomers) {
    this.mockRegistrationTag = mockRegistrationTag;
    initOgelRegistrations(numberOfCustomers);
  }

  private void initOgelRegistrations(int numberOfRegistrations) {
    for (int i = 1; i < numberOfRegistrations + 1; i++) {
      SpireOgelRegistration reg = new SpireOgelRegistration();
      reg.setRegistrationRef(mockRegistrationTag + i);
      mockRegistrations.add(new OgelRegistration(reg));
    }
  }

  public List<OgelRegistration> getRegistrations(String userId, String registrationReference) {
    return mockRegistrations.stream().filter(or -> or.getRegistrationReference().equals(registrationReference)).collect(Collectors.toList());
  }

  public List<OgelRegistration> getRegistrations(String userId) {
    if (noResults) {
      return new ArrayList<>();
    }
    return mockRegistrations;
  }

  public void setNoResults(boolean noResults) {
    this.noResults = noResults;
  }
}

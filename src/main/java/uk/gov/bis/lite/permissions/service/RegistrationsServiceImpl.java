package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import uk.gov.bis.lite.common.spire.client.SpireRequest;
import uk.gov.bis.lite.permissions.model.OgelRegistration;
import uk.gov.bis.lite.permissions.spire.SpireOgelRegistrationClient;
import uk.gov.bis.lite.permissions.spire.model.SpireOgelRegistration;

import java.util.List;
import java.util.stream.Collectors;

public class RegistrationsServiceImpl implements RegistrationsService {

  private SpireOgelRegistrationClient registrationClient;

  @Inject
  public RegistrationsServiceImpl(SpireOgelRegistrationClient registrationClient) {
    this.registrationClient = registrationClient;
  }

  /**
   * Call SpireClient using userId
   */
  public List<OgelRegistration> getRegistrations(String userId) {
    return getSpireOgelRegistrations(userId).stream().map(OgelRegistration::new).collect(Collectors.toList());
  }

  /**
   * Call SpireClient using userId
   * and then filter results based on registrationReference
   */
  public List<OgelRegistration> getRegistrations(String userId, String registrationReference) {
    return getSpireOgelRegistrations(userId).stream()
        .filter(sor -> sor.getRegistrationRef().toUpperCase().equals(registrationReference.toUpperCase()))
        .map(OgelRegistration::new)
        .collect(Collectors.toList());
  }

  private List<SpireOgelRegistration> getSpireOgelRegistrations(String userId) {
    SpireRequest request = registrationClient.createRequest();
    request.addChild("userId", userId);
    return registrationClient.sendRequest(request);
  }

}

package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

import java.util.List;
import java.util.Optional;

public interface RegistrationsService {

  /**
   * Get registrations for userId, filtered to registrationReference.
   * {@link Optional#empty()} implies userId does not exist.
   */
  Optional<List<OgelRegistrationView>> getRegistrations(String userId, String registrationReference);

  /**
   * * Get registrations for userId.
   * {@link Optional#empty()} implies userId does not exist.
   */
  Optional<List<OgelRegistrationView>> getRegistrations(String userId);
}

package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.service.model.RegistrationResult;

public interface RegistrationService {

  /**
   * Get registrations for userId.
   */
  RegistrationResult getRegistrations(String userId);

  /**
   * Get registrations for userId, filtered to registrationReference.
   */
  RegistrationResult getRegistrationByReference(String userId, String reference);

}

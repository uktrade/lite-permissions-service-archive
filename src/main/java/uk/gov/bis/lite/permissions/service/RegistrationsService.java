package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.service.model.registration.MultipleRegistrationResult;
import uk.gov.bis.lite.permissions.service.model.registration.SingleRegistrationResult;

public interface RegistrationsService {

  /**
   * Get registrations for userId, filtered to registrationReference.
   */
  SingleRegistrationResult getRegistration(String userId, String registrationReference);

  /**
   * * Get registrations for userId.
   */
  MultipleRegistrationResult getRegistrations(String userId);
}

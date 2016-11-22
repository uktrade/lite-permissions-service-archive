package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

import java.util.List;

public interface RegistrationsService {

  List<OgelRegistrationView> getRegistrations(String userId, String registrationReference);

  List<OgelRegistrationView> getRegistrations(String userId);
}

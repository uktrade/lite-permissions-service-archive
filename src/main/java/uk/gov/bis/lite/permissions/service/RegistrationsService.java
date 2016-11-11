package uk.gov.bis.lite.permissions.service;


import uk.gov.bis.lite.permissions.model.OgelRegistration;

import java.util.List;

public interface RegistrationsService {

  List<OgelRegistration> getRegistrations(String userId, String registrationReference);

  List<OgelRegistration> getRegistrations(String userId);
}

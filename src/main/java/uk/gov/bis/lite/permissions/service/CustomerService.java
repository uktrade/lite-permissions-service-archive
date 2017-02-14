package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.Optional;

public interface CustomerService {

  Optional<String> getOrCreateCustomer(OgelSubmission sub);

  Optional<String> createSite(OgelSubmission sub);

  boolean updateUserRole(OgelSubmission sub);

}

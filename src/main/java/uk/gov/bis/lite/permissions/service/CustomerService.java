package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.Optional;

public interface CustomerService {

  Optional<String> getOrCreateCustomer(OgelSubmission sub);

  Optional<String> createCustomer(OgelSubmission sub);

  Optional<String> createSite(OgelSubmission sub);

  Optional<String> getCustomerIdByCompanyNumber(String companyNumber);

  boolean updateUserRole(OgelSubmission sub);

}

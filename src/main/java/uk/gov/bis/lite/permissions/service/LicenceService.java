package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.service.model.LicenceResult;

public interface LicenceService {

  LicenceResult getLicenceByRef(String userId, String reference);

  LicenceResult getAllLicences(String userId);

  LicenceResult getLicencesByType(String userId, String type);
}

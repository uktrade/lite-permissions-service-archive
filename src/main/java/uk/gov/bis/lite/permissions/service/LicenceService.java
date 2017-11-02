package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.service.model.SingleLicenceResult;
import uk.gov.bis.lite.permissions.service.model.LicencesResult;

public interface LicenceService {
  enum LicenceTypeParam {
    SIEL,
    OIEL
  }

  SingleLicenceResult getLicence(String userId, String reference);

  LicencesResult getLicences(String userId);

  LicencesResult getLicences(String userId, LicenceTypeParam type);
}

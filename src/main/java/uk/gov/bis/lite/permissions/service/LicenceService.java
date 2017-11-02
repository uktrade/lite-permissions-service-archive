package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.service.model.SingleLicenceResult;
import uk.gov.bis.lite.permissions.service.model.MultipleLicenceResult;

public interface LicenceService {
  enum LicenceTypeParam {
    SIEL,
    OIEL
  }

  SingleLicenceResult getLicence(String userId, String reference);

  MultipleLicenceResult getLicences(String userId);

  MultipleLicenceResult getLicences(String userId, LicenceTypeParam type);
}

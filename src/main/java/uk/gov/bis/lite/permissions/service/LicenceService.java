package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.api.view.LicenceView;

import java.util.List;
import java.util.Optional;

public interface LicenceService {
  enum LicenceType {
    SIEL,
    OIEL
  }
  Optional<List<LicenceView>> getLicence(String userId, String reference);
  Optional<List<LicenceView>> getLicences(String userId);
  Optional<List<LicenceView>> getLicences(String userId, LicenceType type);
}

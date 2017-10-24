package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.api.view.LicenceView;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LicenceServiceImpl implements LicencesService {
  @Override
  public Optional<List<LicenceView>> getLicence(String userId, String reference) {
    return get();
  }

  @Override
  public Optional<List<LicenceView>> getLicences(String userId) {
    return get();
  }

  @Override
  public Optional<List<LicenceView>> getLicences(String userId, LicenceType type) {
    return get();
  }

  private Optional<List<LicenceView>> get() {
    LicenceView licence = new LicenceView();
    licence.setCountryList(Arrays.asList("UK"));
    licence.setExternalDocumentUrl("http://google.com");
    licence.setSubType("subtype");
    licence.setType("type");
    licence.setSarId("SAR-123");
    licence.setExporterApplicationReference("EXPORTER-123");
    licence.setOriginalApplicationReference("ORIGINAL-123");
    licence.setReference("ref");
    return Optional.of(Arrays.asList(licence));
  }
}

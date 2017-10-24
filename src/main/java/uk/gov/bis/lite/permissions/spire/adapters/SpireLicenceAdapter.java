package uk.gov.bis.lite.permissions.spire.adapters;

import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.api.view.Status;
import uk.gov.bis.lite.permissions.spire.model.SpireLicence;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class SpireLicenceAdapter {
  public static List<LicenceView> adapt(List<SpireLicence> spireLicences) {
    return spireLicences
        .stream()
        .map(sl -> {
          LicenceView licence = new LicenceView();
          licence.setReference(sl.getLicenceReference());
          licence.setOriginalApplicationReference(sl.getOriginalApplicationReference());
          licence.setExporterApplicationReference(sl.getExporterApplicationReference());
          licence.setSarId(sl.getSarId());
          licence.setSiteId(sl.getSiteId());
          licence.setType(sl.getLicenceType());
          licence.setSubType(sl.getLicenceSubType());
          licence.setIssueDate(parseSpireDate(sl.getLicenceIssueDate()));
          licence.setExpiryDate(parseSpireDate(sl.getLicenceExpiryDate()));
          licence.setStatus(Status.getEnumByValue(sl.getLicenceStatus())
              .orElseThrow(() -> new SpireLicenceAdapterException("Unknown status: \"" + sl.getLicenceStatus() + "\"")));
          licence.setExternalDocumentUrl(sl.getExternalDocumentUrl());
          licence.setCountryList(sl.getLicenceCountryList());
          return licence;
        })
        .collect(Collectors.toList());
  }

  static LocalDate parseSpireDate(String spireDate) {
    return LocalDate.parse(spireDate, DateTimeFormatter.ofPattern("yyyy-MMM-dd"));
  }
}

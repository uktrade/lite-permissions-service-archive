package uk.gov.bis.lite.permissions.spire.adapters;

import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.api.view.Status;
import uk.gov.bis.lite.permissions.spire.model.SpireLicence;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class SpireLicenceAdapter {

  public static LicenceView adapt(SpireLicence spireLicence) {
    LicenceView licence = new LicenceView();
    licence.setReference(spireLicence.getLicenceReference());
    licence.setOriginalApplicationReference(spireLicence.getOriginalApplicationReference());
    licence.setExporterApplicationReference(spireLicence.getExporterApplicationReference());
    licence.setSarId(spireLicence.getSarId());
    licence.setSiteId(spireLicence.getSiteId());
    licence.setType(spireLicence.getLicenceType());
    licence.setSubType(spireLicence.getLicenceSubType());
    licence.setIssueDate(parseSpireDate(spireLicence.getLicenceIssueDate()));
    licence.setExpiryDate(parseSpireDate(spireLicence.getLicenceExpiryDate()));
    licence.setStatus(Status.getEnumByValue(spireLicence.getLicenceStatus())
        .orElseThrow(() -> new SpireLicenceAdapterException("Unknown status: \"" + spireLicence.getLicenceStatus() + "\"")));
    licence.setExternalDocumentUrl(spireLicence.getExternalDocumentUrl());
    licence.setCountryList(spireLicence.getLicenceCountryList());
    return licence;
  }

  static LocalDate parseSpireDate(String spireDate) {
    return LocalDate.parse(spireDate, DateTimeFormatter.ofPattern("yyyy-MMM-dd"));
  }
}

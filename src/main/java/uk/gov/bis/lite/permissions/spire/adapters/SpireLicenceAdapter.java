package uk.gov.bis.lite.permissions.spire.adapters;

import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.api.view.Status;
import uk.gov.bis.lite.permissions.spire.model.SpireLicence;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class SpireLicenceAdapter {

  public static LicenceView adapt(SpireLicence spireLicence) {
    LicenceView licence = new LicenceView();
    licence.setReference(spireLicence.getReference());
    licence.setOriginalApplicationReference(spireLicence.getOriginalApplicationReference());
    licence.setExporterApplicationReference(spireLicence.getExporterApplicationReference());
    licence.setSarId(spireLicence.getSarId());
    licence.setSiteId(spireLicence.getSiteId());
    licence.setType(spireLicence.getType());
    licence.setSubType(spireLicence.getSubType());
    licence.setIssueDate(parseSpireDate(spireLicence.getIssueDate()));
    licence.setExpiryDate(parseSpireDate(spireLicence.getExpiryDate()));
    if (!StringUtils.isEmpty(spireLicence.getStatus())) {
      licence.setStatus(Status.getEnumByValue(spireLicence.getStatus())
          .orElseThrow(() -> new SpireLicenceAdapterException("Unknown status: \"" + spireLicence.getStatus() + "\"")));
    }
    licence.setExternalDocumentUrl(spireLicence.getExternalDocumentUrl());
    licence.setCountryList(spireLicence.getCountryList());
    return licence;
  }

  /**
   * Parses dates matching the SPIRE format, i.e 2000-JAN-01
   * @param spireDate
   * @return
   */
  static LocalDate parseSpireDate(String spireDate) {
    if (StringUtils.isEmpty(spireDate)) {
      return null;
    }
    DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("uuuu-MMM-dd") // TODO Validate this date format with SPIRE
        .toFormatter(Locale.ENGLISH);
    try {
      return LocalDate.parse(spireDate, dateTimeFormatter);
    } catch (DateTimeParseException e) {
      throw new SpireLicenceAdapterException(String.format("Unexpected date format: \"%s\"", spireDate));
    }
  }
}

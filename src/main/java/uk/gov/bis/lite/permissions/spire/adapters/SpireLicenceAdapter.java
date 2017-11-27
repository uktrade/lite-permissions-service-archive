package uk.gov.bis.lite.permissions.spire.adapters;

import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.spire.model.SpireLicence;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class SpireLicenceAdapter {

  public static LicenceView adapt(SpireLicence spireLicence) {
    LicenceView licence = new LicenceView();
    licence.setLicenceRef(spireLicence.getReference());
    licence.setOriginalAppId(spireLicence.getOriginalApplicationReference());
    licence.setOriginalExporterRef(spireLicence.getExporterApplicationReference());
    licence.setCustomerId(spireLicence.getSarId());
    licence.setSiteId(spireLicence.getSiteId());
    licence.setType(parseSpireEnum(LicenceView.Type.class, spireLicence.getType()));
    licence.setSubType(null); // TODO Convert to parseSpireEnum when values for LicenceView.SubType are known
    licence.setIssueDate(parseSpireDate(spireLicence.getIssueDate()));
    licence.setExpiryDate(parseSpireDate(spireLicence.getExpiryDate()));
    licence.setStatus(parseSpireEnum(LicenceView.Status.class, spireLicence.getStatus()));
    licence.setExternalDocumentUrl(spireLicence.getExternalDocumentUrl());
    licence.setCountryList(spireLicence.getCountryList());
    return licence;
  }

  /**
   * Parses dates matching the SPIRE format, i.e 31/12/2000
   *
   * @param spireDate
   * @return
   */
  static LocalDate parseSpireDate(String spireDate) {
    if (StringUtils.isEmpty(spireDate)) {
      return null;
    }
    DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("dd/MM/uuuu")
        .toFormatter(Locale.ENGLISH);
    try {
      return LocalDate.parse(spireDate, dateTimeFormatter);
    } catch (DateTimeParseException e) {
      throw new SpireLicenceAdapterException(String.format("Unexpected date format: \"%s\"", spireDate));
    }
  }

  private static <E extends Enum<E>> E parseSpireEnum(Class<E> enumClass, String value) {
    if (StringUtils.isEmpty(value)) {
      return null;
    } else {
      try {
        return Enum.valueOf(enumClass, value);
      } catch (IllegalArgumentException e) {
        throw new SpireLicenceAdapterException(String.format("Unknown value for %s: \"%s\"", enumClass.getCanonicalName(), value));
      }
    }
  }
}

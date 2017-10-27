package uk.gov.bis.lite.permissions.api.view;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.bis.lite.permissions.spire.adapters.SpireLicenceAdapter;
import uk.gov.bis.lite.permissions.spire.model.SpireLicence;

import java.time.LocalDate;
import java.util.Collections;

/**
 * Test utility generating and asserting LicenceView's and SpireLicence object
 * LicenceViewX is generated from SpireLicenceX
 */
public class LicenceTestUtil {

  @Test
  public void testAllAssertions() throws Exception {
    assertLicenceViewA(generateLicenceViewA());
    assertLicenceViewB(generateLicenceViewB());
    assertLicenceViewC(generateLicenceViewC());

    assertSpireLicenceA(generateSpireLicenceA());
    assertSpireLicenceB(generateSpireLicenceB());
    assertSpireLicenceC(generateSpireLicenceC());

    assertLicenceViewA(SpireLicenceAdapter.adapt(generateSpireLicenceA()));
    assertLicenceViewB(SpireLicenceAdapter.adapt(generateSpireLicenceB()));
    assertLicenceViewC(SpireLicenceAdapter.adapt(generateSpireLicenceC()));
  }

  public static LicenceView generateLicenceViewA() {
    return new LicenceView()
        .setLicenceRef("REF-123")
        .setOriginalAppId("OREF-123")
        .setOriginalExporterRef("EREF-123")
        .setCustomerId("SAR-123")
        .setSiteId("SITE-123")
        .setType("SIEL")
        .setSubType("SUB")
        .setIssueDate(LocalDate.of(2000, 12, 31))
        .setExpiryDate(LocalDate.of(2020, 12, 31))
        .setStatus(Status.ACTIVE)
        .setCountryList(ImmutableList.of("UK"))
        .setExternalDocumentUrl("http://www.example.org");
  }

  public static LicenceView generateLicenceViewB() {
    return new LicenceView()
        .setLicenceRef("REF-456")
        .setOriginalAppId("OREF-456")
        .setOriginalExporterRef("EREF-456")
        .setCustomerId("SAR-456")
        .setSiteId("SITE-456")
        .setType("SIEL")
        .setSubType("SUB")
        .setIssueDate(LocalDate.of(2000, 12, 31))
        .setExpiryDate(LocalDate.of(2020, 12, 31))
        .setStatus(Status.EXHAUSTED)
        .setCountryList(ImmutableList.of("UK"))
        .setExternalDocumentUrl("http://www.example.org");
  }

  public static LicenceView generateLicenceViewC() {
    return new LicenceView()
        .setLicenceRef("REF-789")
        .setOriginalAppId("OREF-789")
        .setOriginalExporterRef("EREF-789")
        .setCustomerId("SAR-789")
        .setSiteId("SITE-789")
        .setType("OIEL")
        .setSubType("SUB")
        .setIssueDate(LocalDate.of(2000, 12, 31))
        .setExpiryDate(LocalDate.of(2020, 12, 31))
        .setStatus(Status.EXPIRED)
        .setCountryList(Collections.emptyList())
        .setExternalDocumentUrl("http://www.example.org");
  }

  public static void assertLicenceViewA(LicenceView licenceView) {
    assertThat(licenceView.getLicenceRef()).isEqualTo("REF-123");
    assertThat(licenceView.getOriginalAppId()).isEqualTo("OREF-123");
    assertThat(licenceView.getOriginalExporterRef()).isEqualTo("EREF-123");
    assertThat(licenceView.getCustomerId()).isEqualTo("SAR-123");
    assertThat(licenceView.getSiteId()).isEqualTo("SITE-123");
    assertThat(licenceView.getType()).isEqualTo("SIEL");
    assertThat(licenceView.getSubType()).isEqualTo("SUB");
    assertThat(licenceView.getIssueDate()).isEqualTo("2000-12-31");
    assertThat(licenceView.getExpiryDate()).isEqualTo("2020-12-31");
    assertThat(licenceView.getStatus()).isEqualTo(Status.ACTIVE);
    assertThat(licenceView.getCountryList()).containsOnly("UK");
    assertThat(licenceView.getExternalDocumentUrl()).isEqualTo("http://www.example.org");
  }

  public static void assertLicenceViewB(LicenceView licenceView) {
    assertThat(licenceView.getLicenceRef()).isEqualTo("REF-456");
    assertThat(licenceView.getOriginalAppId()).isEqualTo("OREF-456");
    assertThat(licenceView.getOriginalExporterRef()).isEqualTo("EREF-456");
    assertThat(licenceView.getCustomerId()).isEqualTo("SAR-456");
    assertThat(licenceView.getSiteId()).isEqualTo("SITE-456");
    assertThat(licenceView.getType()).isEqualTo("SIEL");
    assertThat(licenceView.getSubType()).isEqualTo("SUB");
    assertThat(licenceView.getIssueDate()).isEqualTo("2000-12-31");
    assertThat(licenceView.getExpiryDate()).isEqualTo("2020-12-31");
    assertThat(licenceView.getStatus()).isEqualTo(Status.EXHAUSTED);
    assertThat(licenceView.getCountryList()).containsOnly("UK");
    assertThat(licenceView.getExternalDocumentUrl()).isEqualTo("http://www.example.org");
  }


  public static void assertLicenceViewC(LicenceView licenceView) {
    assertThat(licenceView.getLicenceRef()).isEqualTo("REF-789");
    assertThat(licenceView.getOriginalAppId()).isEqualTo("OREF-789");
    assertThat(licenceView.getOriginalExporterRef()).isEqualTo("EREF-789");
    assertThat(licenceView.getCustomerId()).isEqualTo("SAR-789");
    assertThat(licenceView.getSiteId()).isEqualTo("SITE-789");
    assertThat(licenceView.getType()).isEqualTo("OIEL");
    assertThat(licenceView.getSubType()).isEqualTo("SUB");
    assertThat(licenceView.getIssueDate()).isEqualTo("2000-12-31");
    assertThat(licenceView.getExpiryDate()).isEqualTo("2020-12-31");
    assertThat(licenceView.getStatus()).isEqualTo(Status.EXPIRED);
    assertThat(licenceView.getCountryList()).isEmpty();
    assertThat(licenceView.getExternalDocumentUrl()).isEqualTo("http://www.example.org");
  }

  public static SpireLicence generateSpireLicenceA() {
    return new SpireLicence()
        .setReference("REF-123")
        .setOriginalApplicationReference("OREF-123")
        .setExporterApplicationReference("EREF-123")
        .setSarId("SAR-123")
        .setSiteId("SITE-123")
        .setType("SIEL")
        .setSubType("SUB")
        .setIssueDate("31/12/2000")
        .setExpiryDate("31/12/2020")
        .setStatus("ACTIVE")
        .setCountryList(ImmutableList.of("UK"))
        .setExternalDocumentUrl("http://www.example.org");
  }

  public static SpireLicence generateSpireLicenceB() {
    return new SpireLicence()
        .setReference("REF-456")
        .setOriginalApplicationReference("OREF-456")
        .setExporterApplicationReference("EREF-456")
        .setSarId("SAR-456")
        .setSiteId("SITE-456")
        .setType("SIEL")
        .setSubType("SUB")
        .setIssueDate("31/12/2000")
        .setExpiryDate("31/12/2020")
        .setStatus("EXHAUSTED")
        .setCountryList(ImmutableList.of("UK"))
        .setExternalDocumentUrl("http://www.example.org");
  }

  public static SpireLicence generateSpireLicenceC() {
    return new SpireLicence()
        .setReference("REF-789")
        .setOriginalApplicationReference("OREF-789")
        .setExporterApplicationReference("EREF-789")
        .setSarId("SAR-789")
        .setSiteId("SITE-789")
        .setType("OIEL")
        .setSubType("SUB")
        .setIssueDate("31/12/2000")
        .setExpiryDate("31/12/2020")
        .setStatus("EXPIRED")
        .setCountryList(Collections.emptyList())
        .setExternalDocumentUrl("http://www.example.org");
  }

  public static void assertSpireLicenceA(SpireLicence spireLicence) {
    assertThat(spireLicence.getReference()).isEqualTo("REF-123");
    assertThat(spireLicence.getOriginalApplicationReference()).isEqualTo("OREF-123");
    assertThat(spireLicence.getExporterApplicationReference()).isEqualTo("EREF-123");
    assertThat(spireLicence.getSarId()).isEqualTo("SAR-123");
    assertThat(spireLicence.getSiteId()).isEqualTo("SITE-123");
    assertThat(spireLicence.getType()).isEqualTo("SIEL");
    assertThat(spireLicence.getSubType()).isEqualTo("SUB");
    assertThat(spireLicence.getIssueDate()).isEqualTo("31/12/2000");
    assertThat(spireLicence.getExpiryDate()).isEqualTo("31/12/2020");
    assertThat(spireLicence.getStatus()).isEqualTo("ACTIVE");
    assertThat(spireLicence.getCountryList()).containsOnly("UK");
    assertThat(spireLicence.getExternalDocumentUrl()).isEqualTo("http://www.example.org");
  }

  public static void assertSpireLicenceB(SpireLicence spireLicence) {
    assertThat(spireLicence.getReference()).isEqualTo("REF-456");
    assertThat(spireLicence.getOriginalApplicationReference()).isEqualTo("OREF-456");
    assertThat(spireLicence.getExporterApplicationReference()).isEqualTo("EREF-456");
    assertThat(spireLicence.getSarId()).isEqualTo("SAR-456");
    assertThat(spireLicence.getSiteId()).isEqualTo("SITE-456");
    assertThat(spireLicence.getType()).isEqualTo("SIEL");
    assertThat(spireLicence.getSubType()).isEqualTo("SUB");
    assertThat(spireLicence.getIssueDate()).isEqualTo("31/12/2000");
    assertThat(spireLicence.getExpiryDate()).isEqualTo("31/12/2020");
    assertThat(spireLicence.getStatus()).isEqualTo("EXHAUSTED");
    assertThat(spireLicence.getCountryList()).containsOnly("UK");
    assertThat(spireLicence.getExternalDocumentUrl()).isEqualTo("http://www.example.org");
  }

  public static void assertSpireLicenceC(SpireLicence spireLicence) {
    assertThat(spireLicence.getReference()).isEqualTo("REF-789");
    assertThat(spireLicence.getOriginalApplicationReference()).isEqualTo("OREF-789");
    assertThat(spireLicence.getExporterApplicationReference()).isEqualTo("EREF-789");
    assertThat(spireLicence.getSarId()).isEqualTo("SAR-789");
    assertThat(spireLicence.getSiteId()).isEqualTo("SITE-789");
    assertThat(spireLicence.getType()).isEqualTo("OIEL");
    assertThat(spireLicence.getSubType()).isEqualTo("SUB");
    assertThat(spireLicence.getIssueDate()).isEqualTo("31/12/2000");
    assertThat(spireLicence.getExpiryDate()).isEqualTo("31/12/2020");
    assertThat(spireLicence.getStatus()).isEqualTo("EXPIRED");
    assertThat(spireLicence.getCountryList()).isEmpty();
    assertThat(spireLicence.getExternalDocumentUrl()).isEqualTo("http://www.example.org");
  }
}

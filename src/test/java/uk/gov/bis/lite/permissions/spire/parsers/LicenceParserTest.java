package uk.gov.bis.lite.permissions.spire.parsers;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertSpireLicenceA;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertSpireLicenceB;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertSpireLicenceC;

import org.junit.Before;
import org.junit.Test;
import uk.gov.bis.lite.common.spire.client.SpireResponse;
import uk.gov.bis.lite.common.spire.client.parser.SpireParser;
import uk.gov.bis.lite.permissions.spire.model.SpireLicence;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

public class LicenceParserTest {
  private SpireParser<List<SpireLicence>> parser;

  @Before
  public void setUp() throws Exception {
    parser = new LicenceParser();
  }

  static SpireResponse createSpireResponse(String soapMessageString) throws Exception {
    InputStream is = new ByteArrayInputStream(soapMessageString.getBytes());
    SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
    return new SpireResponse(soapMessage);
  }

  @Test
  public void singleLicenceTest() throws Exception {
    SpireResponse response = createSpireResponse(fixture("fixture/soap/SPIRE_LICENCES/singleLicence.xml"));

    List<SpireLicence> spireLicences = parser.parseResponse(response);
    assertThat(spireLicences).hasSize(1);

    SpireLicence sl = spireLicences.get(0);
    assertThat(sl.getReference()).isEqualTo("REF-123");
    assertThat(sl.getOriginalApplicationReference()).isEqualTo("OREF-123");
    assertThat(sl.getExporterApplicationReference()).isEqualTo("EREF-123");
    assertThat(sl.getSarId()).isEqualTo("SAR-123");
    assertThat(sl.getSiteId()).isEqualTo("SITE-123");
    assertThat(sl.getType()).isEqualTo("SIEL");
    assertThat(sl.getSubType()).isEqualTo("SUB");
    assertThat(sl.getIssueDate()).isEqualTo("31/12/2000");
    assertThat(sl.getExpiryDate()).isEqualTo("31/12/2020");
    assertThat(sl.getStatus()).isEqualTo("ACTIVE");
    assertThat(sl.getCountryList()).containsOnly("CTRY0");
    assertThat(sl.getExternalDocumentUrl()).isEqualTo("http://www.example.org");
  }

  @Test
  public void multipleLicenceTest() throws Exception {
    SpireResponse response = createSpireResponse(fixture("fixture/soap/SPIRE_LICENCES/multipleLicences.xml"));

    List<SpireLicence> spireLicences = parser.parseResponse(response);
    assertThat(spireLicences).hasSize(3);

    assertSpireLicenceA(spireLicences.get(0));
    assertSpireLicenceB(spireLicences.get(1));
    assertSpireLicenceC(spireLicences.get(2));
  }

  @Test
  public void noLicencesTest() throws Exception {
    SpireResponse response = createSpireResponse(fixture("fixture/soap/SPIRE_LICENCES/noLicences.xml"));

    List<SpireLicence> spireLicences = parser.parseResponse(response);
    assertThat(spireLicences).isEmpty();
  }

  @Test
  public void unhandledErrorTest() throws Exception {
    SpireResponse response = createSpireResponse(fixture("fixture/soap/SPIRE_LICENCES/unhandledError.xml"));

    List<SpireLicence> spireLicences = parser.parseResponse(response);
    assertThat(spireLicences).isEmpty();
  }

  @Test
  public void userIdDoesNotExistTest() throws Exception {
    SpireResponse response = createSpireResponse(fixture("fixture/soap/SPIRE_LICENCES/userIdDoesNotExist.xml"));

    List<SpireLicence> spireLicences = parser.parseResponse(response);
    assertThat(spireLicences).isEmpty();
  }
}
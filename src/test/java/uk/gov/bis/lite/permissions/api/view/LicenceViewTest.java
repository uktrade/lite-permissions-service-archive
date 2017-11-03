package uk.gov.bis.lite.permissions.api.view;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertLicenceViewA;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertLicenceViewB;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertLicenceViewC;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.generateLicenceViewA;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.generateLicenceViewB;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.generateLicenceViewC;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

public class LicenceViewTest {

  private ObjectMapper mapper;

  @Before
  public void setUp() throws Exception {
    mapper = new ObjectMapper();
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.registerModule(new JavaTimeModule());
  }

  @Test
  public void localDateSerialization() throws Exception {
    String json = mapper.writeValueAsString(LocalDate.of(2000,12,31)); // "2000-12-31"
    LocalDate localDate = mapper.readValue(json, LocalDate.class);
    assertThat(localDate).isEqualTo("2000-12-31");
  }

  @Test
  public void testLicenceViewA() throws Exception {
    String json = mapper.writeValueAsString(generateLicenceViewA());
    LicenceView licenceView = mapper.readValue(json, LicenceView.class);
    assertLicenceViewA(licenceView);
  }

  @Test
  public void testLicenceViewB() throws Exception {
    String json = mapper.writeValueAsString(generateLicenceViewB());
    LicenceView licenceView = mapper.readValue(json, LicenceView.class);
    assertLicenceViewB(licenceView);
  }

  @Test
  public void testLicenceViewC() throws Exception {
    String json = mapper.writeValueAsString(generateLicenceViewC());
    LicenceView licenceView = mapper.readValue(json, LicenceView.class);
    assertLicenceViewC(licenceView);
  }
}
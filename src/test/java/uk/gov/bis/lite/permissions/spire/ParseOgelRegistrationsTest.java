package uk.gov.bis.lite.permissions.spire;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.bis.lite.permissions.spire.model.SpireOgelRegistration;
import uk.gov.bis.lite.permissions.spire.parsers.OgelRegistrationParser;

import java.util.List;

public class ParseOgelRegistrationsTest extends SpireParseTest {

  @Test
  public void testOgelRegistrationParser() {
    List<SpireOgelRegistration> regs = new OgelRegistrationParser().parseResponse(createSpireResponse("spire/ogelRegistrations.xml"));
    assertThat(regs).hasSize(3);
    assertThat(regs).extracting("sarRef").contains("SAR1", "SAR2", "SAR2");
    assertThat(regs).filteredOn(comp -> comp.getStatus().equals("EXTANT")).hasSize(1);
  }

}

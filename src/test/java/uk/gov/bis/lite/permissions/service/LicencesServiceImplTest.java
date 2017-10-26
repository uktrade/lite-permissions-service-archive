package uk.gov.bis.lite.permissions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertLicenceViewA;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.assertLicenceViewB;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.generateSpireLicenceA;
import static uk.gov.bis.lite.permissions.api.view.LicenceTestUtil.generateSpireLicenceB;

import org.junit.Test;
import uk.gov.bis.lite.common.spire.client.SpireRequest;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.spire.clients.SpireLicencesClient;
import uk.gov.bis.lite.permissions.spire.exceptions.SpireUserNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LicencesServiceImplTest {

  private SpireLicencesClient client = mock(SpireLicencesClient.class);

  private LicencesServiceImpl service = new LicencesServiceImpl(client);

  @Test
  public void getLicencesSingleTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceA()));

    Optional<List<LicenceView>> licencesOpt = service.getLicences("123456");
    assertThat(licencesOpt).isPresent();
    List<LicenceView> licences = licencesOpt.get();
    assertThat(licences).hasSize(1);
    assertLicenceViewA(licences.get(0));
  }

  @Test
  public void getLicencesMultipleTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceB(), generateSpireLicenceA()));

    Optional<List<LicenceView>> licencesOpt = service.getLicences("123456");
    assertThat(licencesOpt).isPresent();
    List<LicenceView> licences = licencesOpt.get();
    assertThat(licences).hasSize(2);
    // Ordering tested here as REF-123 (Licence A) < REF-456 (Licence B)
    assertLicenceViewB(licences.get(1));
    assertLicenceViewA(licences.get(0));
  }

  @Test
  public void getLicencesNoneTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Collections.emptyList());

    Optional<List<LicenceView>> licencesOpt = service.getLicences("123456");
    assertThat(licencesOpt).isPresent();
    List<LicenceView> licences = licencesOpt.get();
    assertThat(licences).isEmpty();
  }

  @Test
  public void getLicencesUserDoesNotExistTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any())).thenThrow(new SpireUserNotFoundException("User not found"));

    Optional<List<LicenceView>> licencesOpt = service.getLicences("123456");
    assertThat(licencesOpt).isEmpty();
  }

  @Test
  public void getLicenceRefMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceA()));

    Optional<List<LicenceView>> licencesOpt = service.getLicence("123456", "REF-123");
    assertThat(licencesOpt).isPresent();
    List<LicenceView> licences = licencesOpt.get();
    assertThat(licences).hasSize(1);
    assertLicenceViewA(licences.get(0));
  }

  @Test
  public void getLicenceRefNoMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceA()));

    Optional<List<LicenceView>> licencesOpt = service.getLicence("123456", "REF-9999999");
    assertThat(licencesOpt).isPresent();
    List<LicenceView> licences = licencesOpt.get();
    assertThat(licences).isEmpty();
  }

  @Test
  public void getLicenceRefUserDoesNotExistTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any())).thenThrow(new SpireUserNotFoundException("User not found"));

    Optional<List<LicenceView>> licencesOpt = service.getLicence("123456", "REF-123");
    assertThat(licencesOpt).isEmpty();
  }

  @Test
  public void getLicenceTypeMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceA()));

    Optional<List<LicenceView>> licencesOpt = service.getLicences("123456", LicencesService.LicenceType.SIEL);
    assertThat(licencesOpt).isPresent();
    List<LicenceView> licences = licencesOpt.get();
    assertThat(licences).hasSize(1);
    assertLicenceViewA(licences.get(0));
  }

  @Test
  public void getLicenceTypeNoMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceA()));

    Optional<List<LicenceView>> licencesOpt = service.getLicences("123456", LicencesService.LicenceType.OIEL);
    assertThat(licencesOpt).isPresent();
    List<LicenceView> licences = licencesOpt.get();
    assertThat(licences).isEmpty();
  }

  @Test
  public void getLicenceTypeUserDoesNotExistTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any())).thenThrow(new SpireUserNotFoundException("User not found"));

    Optional<List<LicenceView>> licencesOpt = service.getLicences("123456", LicencesService.LicenceType.SIEL);
    assertThat(licencesOpt).isEmpty();
  }
}
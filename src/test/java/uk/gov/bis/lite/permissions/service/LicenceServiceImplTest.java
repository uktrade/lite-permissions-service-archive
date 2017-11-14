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
import uk.gov.bis.lite.permissions.service.model.Status;
import uk.gov.bis.lite.permissions.service.model.licence.MultipleLicenceResult;
import uk.gov.bis.lite.permissions.service.model.licence.SingleLicenceResult;
import uk.gov.bis.lite.permissions.spire.clients.SpireLicencesClient;
import uk.gov.bis.lite.permissions.spire.exceptions.SpireUserNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LicenceServiceImplTest {

  private SpireLicencesClient client = mock(SpireLicencesClient.class);

  private LicenceServiceImpl service = new LicenceServiceImpl(client);

  @Test
  public void getLicencesSingleTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceA()));

    MultipleLicenceResult licencesResult = service.getLicences("123456");
    assertThat(licencesResult.getStatus()).isEqualTo(Status.OK);
    List<LicenceView> licences = licencesResult.getLicenceViews();
    assertThat(licences).hasSize(1);
    assertLicenceViewA(licences.get(0));
  }

  @Test
  public void getLicencesMultipleTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceB(), generateSpireLicenceA()));

    MultipleLicenceResult licencesResult = service.getLicences("123456");
    assertThat(licencesResult.getStatus()).isEqualTo(Status.OK);
    List<LicenceView> licences = licencesResult.getLicenceViews();
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

    MultipleLicenceResult licencesResult = service.getLicences("123456");
    assertThat(licencesResult.getStatus()).isEqualTo(Status.OK);
    List<LicenceView> licences = licencesResult.getLicenceViews();
    assertThat(licences).isEmpty();
  }

  @Test
  public void getLicencesUserDoesNotExistTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any())).thenThrow(new SpireUserNotFoundException("User not found"));

    MultipleLicenceResult licencesResult = service.getLicences("123456");
    assertThat(licencesResult.getStatus()).isEqualTo(Status.USER_ID_NOT_FOUND);
    assertThat(licencesResult.getLicenceViews()).isEmpty();
  }

  @Test
  public void getLicenceRefMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceA()));

    SingleLicenceResult licenceResult = service.getLicence("123456", "REF-123");
    assertThat(licenceResult.getStatus()).isEqualTo(Status.OK);
    assertLicenceViewA(licenceResult.getLicenceView().get());
  }

  @Test
  public void getLicenceRefNoMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Collections.emptyList());

    SingleLicenceResult licenceResult = service.getLicence("123456", "REF-9999999");
    assertThat(licenceResult.getStatus()).isEqualTo(Status.OK);
    assertThat(licenceResult.getLicenceView().isPresent()).isFalse();
  }

  @Test
  public void getLicenceRefUserDoesNotExistTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any())).thenThrow(new SpireUserNotFoundException("User not found"));

    SingleLicenceResult licenceResult = service.getLicence("123456", "REF-123");
    assertThat(licenceResult.getStatus()).isEqualTo(Status.USER_ID_NOT_FOUND);
    assertThat(licenceResult.getLicenceView()).isEmpty();
  }

  @Test
  public void getLicenceTypeMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceA()));

    MultipleLicenceResult licencesResult = service.getLicences("123456", LicenceService.LicenceTypeParam.SIEL);
    assertThat(licencesResult.getStatus()).isEqualTo(Status.OK);
    List<LicenceView> licences = licencesResult.getLicenceViews();
    assertThat(licences).hasSize(1);
    assertLicenceViewA(licences.get(0));
  }

  @Test
  public void getLicenceTypeNoMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Collections.emptyList());

    MultipleLicenceResult licencesResult = service.getLicences("123456", LicenceService.LicenceTypeParam.OIEL);
    assertThat(licencesResult.getStatus()).isEqualTo(Status.OK);
    List<LicenceView> licences = licencesResult.getLicenceViews();
    assertThat(licences).isEmpty();
  }

  @Test
  public void getLicenceTypeUserDoesNotExistTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any())).thenThrow(new SpireUserNotFoundException("User not found"));

    MultipleLicenceResult licencesResult = service.getLicences("123456", LicenceService.LicenceTypeParam.SIEL);
    assertThat(licencesResult.getStatus()).isEqualTo(Status.USER_ID_NOT_FOUND);
    assertThat(licencesResult.getLicenceViews()).isEmpty();
  }
}
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
import uk.gov.bis.lite.permissions.service.model.SingleLicenceResult;
import uk.gov.bis.lite.permissions.service.model.MultipleLicenceResult;
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
    assertThat(licencesResult.isOk()).isTrue();
    List<LicenceView> licences = licencesResult.getResult();
    assertThat(licences).hasSize(1);
    assertLicenceViewA(licences.get(0));
  }

  @Test
  public void getLicencesMultipleTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceB(), generateSpireLicenceA()));

    MultipleLicenceResult licencesResult = service.getLicences("123456");
    assertThat(licencesResult.isOk()).isTrue();
    List<LicenceView> licences = licencesResult.getResult();
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
    assertThat(licencesResult.isOk()).isTrue();
    List<LicenceView> licences = licencesResult.getResult();
    assertThat(licences).isEmpty();
  }

  @Test
  public void getLicencesUserDoesNotExistTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any())).thenThrow(new SpireUserNotFoundException("User not found"));

    MultipleLicenceResult licencesResult = service.getLicences("123456");
    assertThat(licencesResult.isOk()).isFalse();
    assertThat(licencesResult.getStatus()).isEqualTo(MultipleLicenceResult.Status.USER_ID_NOT_FOUND);
    assertThat(licencesResult.getResult()).isNull();
  }

  @Test
  public void getLicenceRefMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceA()));

    SingleLicenceResult licenceResult = service.getLicence("123456", "REF-123");
    assertThat(licenceResult.isOk()).isTrue();
    assertLicenceViewA(licenceResult.getResult().get());
  }

  @Test
  public void getLicenceRefNoMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Collections.emptyList());

    SingleLicenceResult licenceResult = service.getLicence("123456", "REF-9999999");
    assertThat(licenceResult.isOk()).isTrue();
    assertThat(licenceResult.getResult().isPresent()).isFalse();
  }

  @Test
  public void getLicenceRefUserDoesNotExistTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any())).thenThrow(new SpireUserNotFoundException("User not found"));

    SingleLicenceResult licenceResult = service.getLicence("123456", "REF-123");
    assertThat(licenceResult.isOk()).isFalse();
    assertThat(licenceResult.getStatus()).isEqualTo(SingleLicenceResult.Status.USER_ID_NOT_FOUND);
    assertThat(licenceResult.getResult()).isNull();
  }

  @Test
  public void getLicenceTypeMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceA()));

    MultipleLicenceResult licencesResult = service.getLicences("123456", LicenceService.LicenceTypeParam.SIEL);
    assertThat(licencesResult.isOk()).isTrue();
    List<LicenceView> licences = licencesResult.getResult();
    assertThat(licences).hasSize(1);
    assertLicenceViewA(licences.get(0));
  }

  @Test
  public void getLicenceTypeNoMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Collections.emptyList());

    MultipleLicenceResult licencesResult = service.getLicences("123456", LicenceService.LicenceTypeParam.OIEL);
    assertThat(licencesResult.isOk()).isTrue();
    List<LicenceView> licences = licencesResult.getResult();
    assertThat(licences).isEmpty();
  }

  @Test
  public void getLicenceTypeUserDoesNotExistTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any())).thenThrow(new SpireUserNotFoundException("User not found"));

    MultipleLicenceResult licencesResult = service.getLicences("123456", LicenceService.LicenceTypeParam.SIEL);
    assertThat(licencesResult.isOk()).isFalse();
    assertThat(licencesResult.getStatus()).isEqualTo(MultipleLicenceResult.Status.USER_ID_NOT_FOUND);
    assertThat(licencesResult.getResult()).isNull();
  }
}
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
import uk.gov.bis.lite.permissions.service.model.LicenceResult;
import uk.gov.bis.lite.permissions.service.model.LicenceTypeParam;
import uk.gov.bis.lite.permissions.service.model.Status;
import uk.gov.bis.lite.permissions.spire.clients.SpireLicencesClient;
import uk.gov.bis.lite.permissions.spire.exceptions.SpireUserNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LicenceServiceTest {

  private final SpireLicencesClient client = mock(SpireLicencesClient.class);

  private final LicenceServiceImpl service = new LicenceServiceImpl(client);

  @Test
  public void getLicencesSingleTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Collections.singletonList(generateSpireLicenceA()));

    LicenceResult licenceResult = service.getAllLicences("123456");
    assertThat(licenceResult.getStatus()).isEqualTo(Status.OK);
    assertThat(licenceResult.getErrorMessage()).isNull();
    assertThat(licenceResult.getLicenceViews()).hasSize(1);
    assertLicenceViewA(licenceResult.getLicenceViews().get(0));
  }

  @Test
  public void getLicencesMultipleTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Arrays.asList(generateSpireLicenceB(), generateSpireLicenceA()));

    LicenceResult licenceResult = service.getAllLicences("123456");
    assertThat(licenceResult.getStatus()).isEqualTo(Status.OK);
    assertThat(licenceResult.getErrorMessage()).isNull();
    List<LicenceView> licences = licenceResult.getLicenceViews();
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

    LicenceResult licenceResult = service.getAllLicences("123456");
    assertThat(licenceResult.getStatus()).isEqualTo(Status.OK);
    assertThat(licenceResult.getErrorMessage()).isNull();
    assertThat(licenceResult.getLicenceViews()).isEmpty();
  }

  @Test
  public void getLicencesUserDoesNotExistTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any())).thenThrow(new SpireUserNotFoundException("User not found"));

    LicenceResult licenceResult = service.getAllLicences("123456");
    assertThat(licenceResult.getStatus()).isEqualTo(Status.USER_ID_NOT_FOUND);
    assertThat(licenceResult.getErrorMessage()).isEqualTo("Unable to find user with user id 123456");
    assertThat(licenceResult.getLicenceViews()).isNull();
  }

  @Test
  public void getLicenceRefMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Collections.singletonList(generateSpireLicenceA()));

    LicenceResult licenceResult = service.getLicenceByRef("123456", "REF-123");
    assertThat(licenceResult.getStatus()).isEqualTo(Status.OK);
    assertThat(licenceResult.getErrorMessage()).isNull();
    assertThat(licenceResult.getLicenceViews()).hasSize(1);
    assertLicenceViewA(licenceResult.getLicenceViews().get(0));
  }

  @Test
  public void getLicenceRefNoMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any())).thenReturn(Collections.emptyList());

    LicenceResult licenceResult = service.getLicenceByRef("123456", "REF-9999999");
    assertThat(licenceResult.getStatus()).isEqualTo(Status.REGISTRATION_NOT_FOUND);
    assertThat(licenceResult.getErrorMessage()).isEqualTo("No licence with reference REF-9999999 found for userId 123456");
    assertThat(licenceResult.getLicenceViews()).isNull();
  }

  @Test
  public void getLicenceRefUserDoesNotExistTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any())).thenThrow(new SpireUserNotFoundException("User not found"));

    LicenceResult licenceResult = service.getLicenceByRef("123456", "REF-123");
    assertThat(licenceResult.getStatus()).isEqualTo(Status.USER_ID_NOT_FOUND);
    assertThat(licenceResult.getErrorMessage()).isEqualTo("Unable to find user with user id 123456");
    assertThat(licenceResult.getLicenceViews()).isNull();
  }

  @Test
  public void getLicenceTypeMatchesTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any()))
        .thenReturn(Collections.singletonList(generateSpireLicenceA()));

    LicenceResult licenceResult = service.getLicencesByType("123456", LicenceTypeParam.SIEL.toString());
    assertThat(licenceResult.getStatus()).isEqualTo(Status.OK);
    assertThat(licenceResult.getErrorMessage()).isNull();
    assertThat(licenceResult.getLicenceViews()).hasSize(1);
    assertLicenceViewA(licenceResult.getLicenceViews().get(0));
  }

  @Test
  public void getLicenceTypeUserDoesNotExistTest() throws Exception {
    when(client.createRequest()).thenReturn(mock(SpireRequest.class));
    when(client.sendRequest(any())).thenThrow(new SpireUserNotFoundException("User not found"));

    LicenceResult licenceResult = service.getLicencesByType("123456", LicenceTypeParam.SIEL.toString());
    assertThat(licenceResult.getStatus()).isEqualTo(Status.USER_ID_NOT_FOUND);
    assertThat(licenceResult.getErrorMessage()).isEqualTo("Unable to find user with user id 123456");
    assertThat(licenceResult.getLicenceViews()).isNull();
  }
}
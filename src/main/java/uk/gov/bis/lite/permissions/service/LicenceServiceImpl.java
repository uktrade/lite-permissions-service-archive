package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import uk.gov.bis.lite.common.spire.client.SpireRequest;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.exception.LicenceServiceException;
import uk.gov.bis.lite.permissions.service.model.Status;
import uk.gov.bis.lite.permissions.service.model.licence.MultipleLicenceResult;
import uk.gov.bis.lite.permissions.service.model.licence.SingleLicenceResult;
import uk.gov.bis.lite.permissions.spire.adapters.SpireLicenceAdapter;
import uk.gov.bis.lite.permissions.spire.clients.SpireLicencesClient;
import uk.gov.bis.lite.permissions.spire.exceptions.SpireUserNotFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class LicenceServiceImpl implements LicenceService {

  private final SpireLicencesClient client;

  @Inject
  public LicenceServiceImpl(SpireLicencesClient client) {
    this.client = client;
  }

  @Override
  public SingleLicenceResult getLicence(String userId, String reference) {
    SpireRequest spireRequest = client.createRequest();
    spireRequest.addChild("userId", userId);
    spireRequest.addChild("reference", reference);
    try {
      List<LicenceView> licences = client.sendRequest(spireRequest)
          .stream()
          .map(SpireLicenceAdapter::adapt)
          .collect(Collectors.toList());
      if (licences.isEmpty()) {
        return new SingleLicenceResult(Status.OK, null);
      } else if (licences.size() == 1) {
        return new SingleLicenceResult(Status.OK, licences.get(0));
      } else {
        throw new LicenceServiceException(String.format("Too many results from spire client, expected 1 but got %d", licences.size()));
      }
    } catch (SpireUserNotFoundException e) {
      return new SingleLicenceResult(Status.USER_ID_NOT_FOUND, null);
    }
  }

  @Override
  public MultipleLicenceResult getLicences(String userId) {
    SpireRequest spireRequest = client.createRequest();
    spireRequest.addChild("userId", userId);
    try {
      List<LicenceView> licenceViews = client.sendRequest(spireRequest)
          .stream()
          .map(SpireLicenceAdapter::adapt)
          .sorted(Comparator.comparing(LicenceView::getLicenceRef))
          .collect(Collectors.toList());
      return new MultipleLicenceResult(Status.OK, licenceViews);
    } catch (SpireUserNotFoundException e) {
      return new MultipleLicenceResult(Status.USER_ID_NOT_FOUND, null);
    }
  }

  @Override
  public MultipleLicenceResult getLicences(String userId, LicenceTypeParam type) {
    SpireRequest spireRequest = client.createRequest();
    spireRequest.addChild("userId", userId);
    spireRequest.addChild("type", type.name());
    try {
      List<LicenceView> licenceViews = client.sendRequest(spireRequest)
          .stream()
          .map(SpireLicenceAdapter::adapt)
          .sorted(Comparator.comparing(LicenceView::getLicenceRef))
          .collect(Collectors.toList());
      return new MultipleLicenceResult(Status.OK, licenceViews);
    } catch (SpireUserNotFoundException e) {
      return new MultipleLicenceResult(Status.USER_ID_NOT_FOUND, null);
    }
  }
}

package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import uk.gov.bis.lite.common.spire.client.SpireRequest;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.service.model.LicenceResult;
import uk.gov.bis.lite.permissions.service.model.LicenceTypeParam;
import uk.gov.bis.lite.permissions.service.model.Status;
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
  public LicenceResult getLicenceByRef(String userId, String reference) {
    SpireRequest spireRequest = client.createRequest();
    spireRequest.addChild("userId", userId);
    spireRequest.addChild("reference", reference);
    try {
      List<LicenceView> licences = client.sendRequest(spireRequest)
          .stream()
          .map(SpireLicenceAdapter::adapt)
          .collect(Collectors.toList());
      if (licences.isEmpty()) {
        String errorMessage = String.format("No licence with reference %s found for userId %s", reference, userId);
        return new LicenceResult(Status.REGISTRATION_NOT_FOUND, errorMessage, null);
      } else if (licences.size() == 1) {
        return new LicenceResult(Status.OK, null, licences);
      } else {
        String errorMessage = String.format("Too many results from spire client, expected 1 but got %d", licences.size());
        return new LicenceResult(Status.TOO_MANY_REGISTRATIONS, errorMessage, null);
      }
    } catch (SpireUserNotFoundException e) {
      return userNotFound(userId);
    }
  }

  @Override
  public LicenceResult getAllLicences(String userId) {
    SpireRequest spireRequest = client.createRequest();
    spireRequest.addChild("userId", userId);
    try {
      List<LicenceView> licenceViews = client.sendRequest(spireRequest)
          .stream()
          .map(SpireLicenceAdapter::adapt)
          .sorted(Comparator.comparing(LicenceView::getLicenceRef))
          .collect(Collectors.toList());
      return new LicenceResult(Status.OK, null, licenceViews);
    } catch (SpireUserNotFoundException e) {
      return userNotFound(userId);
    }
  }

  @Override
  public LicenceResult getLicencesByType(String userId, LicenceTypeParam type) {
    SpireRequest spireRequest = client.createRequest();
    spireRequest.addChild("userId", userId);
    spireRequest.addChild("type", type.name());
    try {
      List<LicenceView> licenceViews = client.sendRequest(spireRequest)
          .stream()
          .map(SpireLicenceAdapter::adapt)
          .sorted(Comparator.comparing(LicenceView::getLicenceRef))
          .collect(Collectors.toList());
      return new LicenceResult(Status.OK, null, licenceViews);
    } catch (SpireUserNotFoundException e) {
      return userNotFound(userId);
    }
  }

  private LicenceResult userNotFound(String userId) {
    String errorMessage = "Unable to find user with user id " + userId;
    return new LicenceResult(Status.USER_ID_NOT_FOUND, errorMessage, null);
  }

}

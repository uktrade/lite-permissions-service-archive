package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.common.spire.client.SpireRequest;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.spire.adapters.SpireLicenceAdapter;
import uk.gov.bis.lite.permissions.spire.clients.SpireLicencesClient;
import uk.gov.bis.lite.permissions.spire.exceptions.SpireUserNotFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class LicenceServiceImpl implements LicenceService {

  private final SpireLicencesClient client;

  @Inject
  public LicenceServiceImpl(SpireLicencesClient client) {
    this.client = client;
  }

  @Override
  public Optional<List<LicenceView>> getLicence(String userId, String reference) {
    SpireRequest spireRequest = client.createRequest();
    spireRequest.addChild("userId", userId);
    try {
      return Optional.of(client.sendRequest(spireRequest)
          .stream()
          .filter(sl -> StringUtils.equalsIgnoreCase(sl.getReference(), reference))
          .map(SpireLicenceAdapter::adapt)
          .sorted(Comparator.comparing(LicenceView::getLicenceRef))
          .collect(Collectors.toList()));
    } catch (SpireUserNotFoundException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<List<LicenceView>> getLicences(String userId) {
    SpireRequest spireRequest = client.createRequest();
    spireRequest.addChild("userId", userId);
    try {
      return Optional.of(client.sendRequest(spireRequest)
          .stream()
          .map(SpireLicenceAdapter::adapt)
          .sorted(Comparator.comparing(LicenceView::getLicenceRef))
          .collect(Collectors.toList()));
    } catch (SpireUserNotFoundException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<List<LicenceView>> getLicences(String userId, LicenceType type) {
    SpireRequest spireRequest = client.createRequest();
    spireRequest.addChild("userId", userId);
    try {
      return Optional.of(client.sendRequest(spireRequest)
          .stream()
          .filter(sl -> StringUtils.equalsIgnoreCase(sl.getType(), type.name()))
          .map(SpireLicenceAdapter::adapt)
          .sorted(Comparator.comparing(LicenceView::getLicenceRef))
          .collect(Collectors.toList()));
    } catch (SpireUserNotFoundException e) {
      return Optional.empty();
    }
  }
}

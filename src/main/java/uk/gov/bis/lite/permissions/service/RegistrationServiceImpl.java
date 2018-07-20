package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.spire.client.SpireRequest;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.model.RegistrationResult;
import uk.gov.bis.lite.permissions.service.model.Status;
import uk.gov.bis.lite.permissions.spire.clients.SpireOgelRegistrationClient;
import uk.gov.bis.lite.permissions.spire.exceptions.SpireUserNotFoundException;
import uk.gov.bis.lite.permissions.spire.model.SpireOgelRegistration;

import java.util.List;
import java.util.stream.Collectors;

public class RegistrationServiceImpl implements RegistrationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationServiceImpl.class);

  private final SpireOgelRegistrationClient registrationClient;

  @Inject
  public RegistrationServiceImpl(SpireOgelRegistrationClient registrationClient) {
    this.registrationClient = registrationClient;
  }

  /**
   * Call SpireClient using userId.
   */
  @Override
  public RegistrationResult getRegistrations(String userId) {
    SpireRequest request = registrationClient.createRequest();
    request.addChild("userId", userId);
    try {
      List<OgelRegistrationView> registrations = registrationClient.sendRequest(request)
          .stream()
          .map(this::getOgelRegistrationView)
          .collect(Collectors.toList());
      return new RegistrationResult(Status.OK, null, registrations);
    } catch (SpireUserNotFoundException e) {
      return userNotFound(userId);
    }
  }

  /**
   * Call SpireClient using userId and registrationReference.
   */
  @Override
  public RegistrationResult getRegistrationByReference(String userId, String reference) {
    SpireRequest request = registrationClient.createRequest();
    request.addChild("userId", userId);
    try {
      List<OgelRegistrationView> registrations = registrationClient.sendRequest(request)
          .stream()
          .filter(sor -> reference.equalsIgnoreCase(sor.getRegistrationRef()))
          .map(this::getOgelRegistrationView)
          .collect(Collectors.toList());
      if (registrations.isEmpty()) {
        String errorMessage = String.format("No licence with reference %s found for userId %s", reference, userId);
        return new RegistrationResult(Status.REGISTRATION_NOT_FOUND, errorMessage, null);
      } else if (registrations.size() == 1) {
        return new RegistrationResult(Status.OK, null, registrations);
      } else {
        String errorMessage = String.format("Too many results from spire client, expected 1 but got %d", registrations.size());
        return new RegistrationResult(Status.TOO_MANY_REGISTRATIONS, errorMessage, null);
      }
    } catch (SpireUserNotFoundException e) {
      return userNotFound(userId);
    }
  }

  private RegistrationResult userNotFound(String userId) {
    String errorMessage = String.format("Unable to find user with user id %s", userId);
    return new RegistrationResult(Status.USER_ID_NOT_FOUND, errorMessage, null);
  }

  private OgelRegistrationView getOgelRegistrationView(SpireOgelRegistration spireOgelRegistration) {
    OgelRegistrationView view = new OgelRegistrationView();
    view.setRegistrationReference(spireOgelRegistration.getRegistrationRef());
    view.setCustomerId(spireOgelRegistration.getSarRef());
    view.setStatus(getStatus(spireOgelRegistration.getStatus()));
    view.setOgelType(spireOgelRegistration.getOgelTypeRef());
    view.setRegistrationDate(spireOgelRegistration.getRegistrationDate());
    view.setSiteId(spireOgelRegistration.getSiteRef());
    return view;
  }

  /**
   * Returns appropriate OgelRegistrationView.Status
   * Defaults to UNKNOWN if status argument not recognised
   */
  private OgelRegistrationView.Status getStatus(String arg) {
    if (EnumUtils.isValidEnum(OgelRegistrationView.Status.class, arg)) {
      return OgelRegistrationView.Status.valueOf(arg);
    } else {
      LOGGER.error("Received unknown OgelRegistrationView status {}", arg);
      return OgelRegistrationView.Status.UNKNOWN;
    }
  }

}

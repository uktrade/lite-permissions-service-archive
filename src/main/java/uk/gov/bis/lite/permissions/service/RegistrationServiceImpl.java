package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.spire.client.SpireRequest;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.exception.OgelRegistrationServiceException;
import uk.gov.bis.lite.permissions.service.model.Status;
import uk.gov.bis.lite.permissions.service.model.registration.MultipleRegistrationResult;
import uk.gov.bis.lite.permissions.service.model.registration.SingleRegistrationResult;
import uk.gov.bis.lite.permissions.spire.clients.SpireOgelRegistrationClient;
import uk.gov.bis.lite.permissions.spire.exceptions.SpireUserNotFoundException;
import uk.gov.bis.lite.permissions.spire.model.SpireOgelRegistration;

import java.util.List;
import java.util.stream.Collectors;

public class RegistrationServiceImpl implements RegistrationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationServiceImpl.class);

  private SpireOgelRegistrationClient registrationClient;

  @Inject
  public RegistrationServiceImpl(SpireOgelRegistrationClient registrationClient) {
    this.registrationClient = registrationClient;
  }

  /**
   * Call SpireClient using userId.
   */
  public MultipleRegistrationResult getRegistrations(String userId) {
    SpireRequest request = registrationClient.createRequest();
    request.addChild("userId", userId);
    try {
      List<OgelRegistrationView> registrations = registrationClient.sendRequest(request)
          .stream()
          .map(this::getOgelRegistrationView)
          .collect(Collectors.toList());
      return new MultipleRegistrationResult(Status.OK, registrations);
    } catch (SpireUserNotFoundException e) {
      return new MultipleRegistrationResult(Status.USER_ID_NOT_FOUND, null);
    }
  }

  /**
   * Call SpireClient using userId and registrationReference.
   */
  public SingleRegistrationResult getRegistration(String userId, String registrationReference) {
    SpireRequest request = registrationClient.createRequest();
    request.addChild("userId", userId);
    try {
      List<OgelRegistrationView> registrations = registrationClient.sendRequest(request)
          .stream()
          .filter(sor -> registrationReference.equalsIgnoreCase(sor.getRegistrationRef()))
          .map(this::getOgelRegistrationView)
          .collect(Collectors.toList());
      if (registrations.isEmpty()) {
        return new SingleRegistrationResult(Status.OK, null);
      } else if (registrations.size() == 1) {
        return new SingleRegistrationResult(Status.OK, registrations.get(0));
      } else {
        throw new OgelRegistrationServiceException(String.format("Too many results from spire client, expected 1 but got %d", registrations.size()));
      }
    } catch (SpireUserNotFoundException e) {
      return new SingleRegistrationResult(Status.USER_ID_NOT_FOUND, null);
    }
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
    OgelRegistrationView.Status status = OgelRegistrationView.Status.UNKNOWN;
    if (EnumUtils.isValidEnum(OgelRegistrationView.Status.class, arg)) {
      status = OgelRegistrationView.Status.valueOf(arg);
    } else {
      LOGGER.error("Received unknown OgelRegistrationView status: {}", arg);
    }
    return status;
  }

}

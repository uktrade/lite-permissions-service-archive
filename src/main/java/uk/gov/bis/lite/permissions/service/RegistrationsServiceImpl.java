package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.spire.client.SpireRequest;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.spire.SpireOgelRegistrationClient;
import uk.gov.bis.lite.permissions.spire.model.SpireOgelRegistration;

import java.util.List;
import java.util.stream.Collectors;

public class RegistrationsServiceImpl implements RegistrationsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationsServiceImpl.class);

  private SpireOgelRegistrationClient registrationClient;

  @Inject
  public RegistrationsServiceImpl(SpireOgelRegistrationClient registrationClient) {
    this.registrationClient = registrationClient;
  }

  /**
   * Call SpireClient using userId
   */
  public List<OgelRegistrationView> getRegistrations(String userId) {
    return getSpireOgelRegistrations(userId).stream().map(this::getOgelRegistrationView).collect(Collectors.toList());
  }

  /**
   * Call SpireClient using userId
   * and then filter results based on registrationReference
   */
  public List<OgelRegistrationView> getRegistrations(String userId, String registrationReference) {
    return getSpireOgelRegistrations(userId).stream()
        .filter(sor -> sor.getRegistrationRef().toUpperCase().equals(registrationReference.toUpperCase()))
        .map(this::getOgelRegistrationView)
        .collect(Collectors.toList());
  }

  private List<SpireOgelRegistration> getSpireOgelRegistrations(String userId) {
    SpireRequest request = registrationClient.createRequest();
    request.addChild("userId", userId);
    return registrationClient.sendRequest(request);
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

package uk.gov.bis.lite.permissions.resource;

import static uk.gov.bis.lite.permissions.resource.ResourceUtil.validateServiceResult;
import static uk.gov.bis.lite.permissions.resource.ResourceUtil.validateUserIdToJwt;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.RegistrationsService;
import uk.gov.bis.lite.permissions.service.model.registration.MultipleRegistrationResult;
import uk.gov.bis.lite.permissions.service.model.registration.SingleRegistrationResult;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("")
public class OgelRegistrationResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelRegistrationResource.class);
  private RegistrationsService registrationsService;

  @Inject
  public OgelRegistrationResource(RegistrationsService registrationsService) {
    this.registrationsService = registrationsService;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/ogel-registrations/user/{userId}")
  public List<OgelRegistrationView> viewOgelRegistrations(@NotNull @PathParam("userId") String userId,
                                                          @QueryParam("registrationReference") String registrationReference,
                                                          @Auth LiteJwtUser user) {
    validateUserIdToJwt(userId, user);
    if (StringUtils.isNotBlank(registrationReference)) {
      return getRegistrationByRef(userId, registrationReference);
    } else {
      return getAllRegistrations(userId);
    }
  }

  private List<OgelRegistrationView> getAllRegistrations(String userId) {
    MultipleRegistrationResult registrations = registrationsService.getRegistrations(userId);
    validateServiceResult(registrations);
    return registrations.getResult();
  }

  private List<OgelRegistrationView> getRegistrationByRef(String userId, String registrationReference) {
    SingleRegistrationResult registration = registrationsService.getRegistration(userId, registrationReference);
    validateServiceResult(registration);
    return registration.getResult()
        .map(ImmutableList::of)
        .orElseThrow(() -> new WebApplicationException(String.format("No licence with ref \"%s\" found " +
            "for userId \"%s\"", registrationReference, userId), Response.Status.NOT_FOUND));
  }
}

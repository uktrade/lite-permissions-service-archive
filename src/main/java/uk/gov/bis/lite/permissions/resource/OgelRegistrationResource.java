package uk.gov.bis.lite.permissions.resource;

import static uk.gov.bis.lite.permissions.resource.ResourceUtil.validateUserIdToJwt;

import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.RegistrationsService;

import java.util.List;
import java.util.Optional;

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

    Optional<List<OgelRegistrationView>> results;

    if (StringUtils.isBlank(registrationReference)) {
      results = registrationsService.getRegistrations(userId);
    } else {
      results = registrationsService.getRegistrations(userId, registrationReference);
    }

    if (results.isPresent()) {
      return results.get();
    } else {
      throw new WebApplicationException(String.format("userId %s not found.", userId), Response.Status.NOT_FOUND);
    }
  }

}

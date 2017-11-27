package uk.gov.bis.lite.permissions.resource;

import static uk.gov.bis.lite.permissions.resource.ResourceUtil.validateServiceStatus;
import static uk.gov.bis.lite.permissions.resource.ResourceUtil.validateUserIdToJwt;

import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.permissions.service.RegistrationService;
import uk.gov.bis.lite.permissions.service.model.RegistrationResult;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("")
public class OgelRegistrationResource {

  private final RegistrationService registrationService;

  @Inject
  public OgelRegistrationResource(RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/ogel-registrations/user/{userId}")
  public List<OgelRegistrationView> viewOgelRegistrations(@NotNull @PathParam("userId") String userId,
                                                          @QueryParam("registrationReference") String registrationReference,
                                                          @Auth LiteJwtUser user) {
    validateUserIdToJwt(userId, user);
    RegistrationResult registrationResult;
    if (StringUtils.isNotBlank(registrationReference)) {
      registrationResult = registrationService.getRegistration(userId, registrationReference);
    } else {
      registrationResult = registrationService.getRegistrations(userId);
    }
    validateServiceStatus(registrationResult.getStatus(), registrationResult.getErrorMessage());
    return registrationResult.getOgelRegistrationViews();
  }

}

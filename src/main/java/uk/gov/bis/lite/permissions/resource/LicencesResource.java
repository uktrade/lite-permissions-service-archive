package uk.gov.bis.lite.permissions.resource;

import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.service.LicenceService;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/licences")
public class LicencesResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicencesResource.class);

  private final LicenceService licenceService;

  @Inject
  public LicencesResource(LicenceService licenceService) {
    this.licenceService = licenceService;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/{userId}")
  public List<LicenceView> viewUserLicences(@PathParam("userId") String userId,
                                            @QueryParam("type") String type,
                                            @QueryParam("ref") String ref,
                                            @Auth LiteJwtUser user) {
    validateUserIdToJwt(userId, user);
    Optional<List<LicenceView>> licences;
    if (ref != null) {
      licences = licenceService.getLicence(userId, ref);
    } else if (type != null) {
      if (StringUtils.equalsIgnoreCase(type, LicenceService.LicenceType.SIEL.name())) {
        licences = licenceService.getLicences(userId, LicenceService.LicenceType.SIEL);
      } else {
        throw new WebApplicationException(String.format("Invalid licence type \"%s\"", type), Response.Status.BAD_REQUEST);
      }
    } else {
      licences = licenceService.getLicences(userId);
    }

    if (licences.isPresent()) {
      return licences.get();
    } else {
      throw new WebApplicationException("User not found.", Response.Status.NOT_FOUND);
    }
  }

  static void validateUserIdToJwt(String userId, LiteJwtUser user) {
    if (!StringUtils.equals(userId, user.getUserId())) {
      throw new WebApplicationException("userId \"" + userId + "\" does not match value supplied in token (" +
          user.getUserId() + ")", Response.Status.UNAUTHORIZED);
    }
  }
}

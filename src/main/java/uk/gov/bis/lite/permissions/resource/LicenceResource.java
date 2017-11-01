package uk.gov.bis.lite.permissions.resource;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.service.LicenceService;
import uk.gov.bis.lite.permissions.service.model.LicenceResult;
import uk.gov.bis.lite.permissions.service.model.LicenceServiceResult;
import uk.gov.bis.lite.permissions.service.model.LicencesResult;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/licences")
public class LicenceResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenceResource.class);

  private final LicenceService licenceService;

  @Inject
  public LicenceResource(LicenceService licenceService) {
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
    if (ref != null) {
      return getLicenceByRef(userId, ref);
    } else if (type != null) {
      return getLicencesByType(userId, type);
    } else {
      return getAllLicences(userId);
    }
  }

  List<LicenceView> getLicenceByRef(String userId, String ref) {
    LicenceResult licenceResult = licenceService.getLicence(userId, ref);
    validateResult(licenceResult);
    if (licenceResult.getResult() == null) {
      return Collections.emptyList();
    } else {
      return ImmutableList.of(licenceResult.getResult());
    }
  }

  List<LicenceView> getAllLicences(String userId) {
    LicencesResult licencesResult = licenceService.getLicences(userId);
    validateResult(licencesResult);
    return licencesResult.getResult();
  }

  List<LicenceView> getLicencesByType(String userId, String type) {
    if (StringUtils.equalsIgnoreCase(type, LicenceService.LicenceTypeParam.SIEL.name())) {
      LicencesResult licencesResult = licenceService.getLicences(userId, LicenceService.LicenceTypeParam.SIEL);
      validateResult(licencesResult);
      return licencesResult.getResult();
    } else {
      throw new WebApplicationException(String.format("Invalid licence type \"%s\"", type), Response.Status.BAD_REQUEST);
    }
  }

  static void validateUserIdToJwt(String userId, LiteJwtUser user) {
    if (!StringUtils.equals(userId, user.getUserId())) {
      throw new WebApplicationException("userId \"" + userId + "\" does not match value supplied in token (" +
          user.getUserId() + ")", Response.Status.UNAUTHORIZED);
    }
  }

  static void validateResult(LicenceServiceResult<?> serviceResult) {
    if (!serviceResult.isOk()) {
      if (serviceResult.getStatus() == LicenceServiceResult.Status.USER_ID_NOT_FOUND) {
        throw new WebApplicationException("User not found.", Response.Status.NOT_FOUND);
      } else {
        LOGGER.warn("Unexpected value for LicenceServiceResult.Status");
      }
    }
  }
}

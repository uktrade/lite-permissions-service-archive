package uk.gov.bis.lite.permissions.resource;

import static uk.gov.bis.lite.permissions.resource.ResourceUtil.validateServiceStatus;
import static uk.gov.bis.lite.permissions.resource.ResourceUtil.validateUserIdToJwt;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.service.LicenceService;
import uk.gov.bis.lite.permissions.service.model.licence.MultipleLicenceResult;
import uk.gov.bis.lite.permissions.service.model.licence.SingleLicenceResult;

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
    SingleLicenceResult licenceResult = licenceService.getLicence(userId, ref);
    validateServiceStatus(licenceResult.getStatus());
    return licenceResult.getLicenceView()
        .map(ImmutableList::of)
        .orElseThrow(() -> new WebApplicationException(String.format("No licence with ref \"%s\" found " +
          "for userId \"%s\"", ref, userId), Response.Status.NOT_FOUND));
  }

  List<LicenceView> getAllLicences(String userId) {
    MultipleLicenceResult licencesResult = licenceService.getLicences(userId);
    validateServiceStatus(licencesResult.getStatus());
    return licencesResult.getLicenceViews();
  }

  List<LicenceView> getLicencesByType(String userId, String type) {
    if (StringUtils.equalsIgnoreCase(type, LicenceService.LicenceTypeParam.SIEL.name())) {
      MultipleLicenceResult licencesResult = licenceService.getLicences(userId, LicenceService.LicenceTypeParam.SIEL);
      validateServiceStatus(licencesResult.getStatus());
      return licencesResult.getLicenceViews();
    } else {
      throw new WebApplicationException(String.format("Invalid licence type \"%s\"", type), Response.Status.BAD_REQUEST);
    }
  }
}

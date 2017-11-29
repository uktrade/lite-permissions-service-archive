package uk.gov.bis.lite.permissions.resource;

import static uk.gov.bis.lite.permissions.resource.ResourceUtil.validateServiceStatus;
import static uk.gov.bis.lite.permissions.resource.ResourceUtil.validateUserIdToJwt;

import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.service.LicenceService;
import uk.gov.bis.lite.permissions.service.model.LicenceResult;
import uk.gov.bis.lite.permissions.service.model.LicenceTypeParam;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/licences")
public class LicenceResource {

  private final LicenceService licenceService;

  @Inject
  public LicenceResource(LicenceService licenceService) {
    this.licenceService = licenceService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/user/{userId}")
  public List<LicenceView> viewUserLicences(@PathParam("userId") String userId,
                                            @QueryParam("type") LicenceTypeParam type,
                                            @QueryParam("licenceReference") String licenceReference,
                                            @Auth LiteJwtUser user) {
    validateUserIdToJwt(userId, user);
    LicenceResult licenceResult;
    if (licenceReference != null) {
      licenceResult = licenceService.getLicenceByRef(userId, licenceReference);
    } else if (type != null) {
      licenceResult = licenceService.getLicencesByType(userId, type);
    } else {
      licenceResult = licenceService.getAllLicences(userId);
    }
    validateServiceStatus(licenceResult.getStatus(), licenceResult.getErrorMessage());
    return licenceResult.getLicenceViews();
  }

}

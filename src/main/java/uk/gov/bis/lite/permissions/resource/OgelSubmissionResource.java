package uk.gov.bis.lite.permissions.resource;

import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.PrincipalImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.api.view.OgelSubmissionView;
import uk.gov.bis.lite.permissions.service.OgelSubmissionService;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("")
public class OgelSubmissionResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelRegistrationResource.class);
  private OgelSubmissionService ogelSubmissionService;

  @Inject
  public OgelSubmissionResource(OgelSubmissionService ogelSubmissionService) {
    this.ogelSubmissionService = ogelSubmissionService;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/ogel-submissions")
  public List<OgelSubmissionView> viewOgelSubmissions(@Auth PrincipalImpl user) {
    return ogelSubmissionService.getOgelSubmissions();
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/ogel-submissions/{id}")
  public OgelSubmissionView viewOgelSubmission(@Auth PrincipalImpl user, @NotNull @PathParam("id") int id) {
    if(!ogelSubmissionService.ogelSubmissionExists(id)) {
      throw new WebApplicationException("Ogel Submission not found", Response.Status.NOT_FOUND);
    }
    return ogelSubmissionService.getOgelSubmission(id);
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/ogel-submissions")
  public Response cancelScheduledSubmissions(@Auth PrincipalImpl user) {
    ogelSubmissionService.cancelScheduledOgelSubmissions();
    return Response.status(Response.Status.OK).build();
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/ogel-submissions/{id}")
  public Response cancelScheduledSubmission(@Auth PrincipalImpl user, @NotNull @PathParam("id") int id) {
    if(!ogelSubmissionService.ogelSubmissionExists(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    ogelSubmissionService.cancelScheduledOgelSubmission(id);
    return Response.status(Response.Status.OK).build();
  }

}


package uk.gov.bis.lite.permissions.resource;

import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.PrincipalImpl;
import uk.gov.bis.lite.permissions.api.view.OgelSubmissionView;
import uk.gov.bis.lite.permissions.service.SubmissionService;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("")
public class OgelSubmissionResource {

  private final SubmissionService submissionService;

  @Inject
  public OgelSubmissionResource(SubmissionService submissionService) {
    this.submissionService = submissionService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/ogel-submissions")
  public List<OgelSubmissionView> viewOgelSubmissions(@Auth PrincipalImpl user, @DefaultValue("PENDING") @QueryParam("filter") String filter) {
    return submissionService.getOgelSubmissions(filter);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/ogel-submissions/{id}")
  public OgelSubmissionView viewOgelSubmission(@Auth PrincipalImpl user, @NotNull @PathParam("id") int id) {
    if (!submissionService.ogelSubmissionExists(id)) {
      throw new WebApplicationException("Ogel Submission not found", Response.Status.NOT_FOUND);
    } else {
      return submissionService.getOgelSubmission(id);
    }
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/ogel-submissions")
  public Response cancelScheduledSubmissions(@Auth PrincipalImpl user) {
    submissionService.cancelPendingScheduledOgelSubmissions();
    return Response.status(Response.Status.OK).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/ogel-submissions/{id}")
  public Response cancelScheduledSubmission(@Auth PrincipalImpl user, @NotNull @PathParam("id") int id) {
    if (!submissionService.ogelSubmissionExists(id)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } else {
      submissionService.cancelScheduledOgelSubmission(id);
      return Response.status(Response.Status.OK).build();
    }
  }

}


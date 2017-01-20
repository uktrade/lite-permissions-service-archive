package uk.gov.bis.lite.permissions.resource;

import com.google.inject.Inject;
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
  public List<OgelSubmissionView> viewOgelSubmissions() {
    LOGGER.info("viewOgelSubmissions");
    return ogelSubmissionService.getOgelSubmissions();
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/ogel-submissions/{id}")
  public OgelSubmissionView viewOgelSubmission(@NotNull @PathParam("id") String id) {
    LOGGER.info("viewOgelSubmission: " + id);
    return ogelSubmissionService.getOgelSubmission(Integer.parseInt(id));
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/ogel-submissions")
  public Response cancelScheduledSubmissions() {
    LOGGER.info("cancelScheduledSubmissions");
    ogelSubmissionService.cancelScheduledOgelSubmissions();
    return Response.status(Response.Status.OK).build();
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/ogel-submissions/{id}")
  public Response cancelScheduledSubmission(@NotNull @PathParam("id") String id) {
    LOGGER.info("cancelScheduledSubmission: " + id);
    ogelSubmissionService.cancelScheduledOgelSubmission(Integer.parseInt(id));
    return Response.status(Response.Status.OK).build();
  }

}


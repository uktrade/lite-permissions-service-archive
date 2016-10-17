package uk.gov.bis.lite.permissions.resource;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.model.register.RegisterOgel;
import uk.gov.bis.lite.permissions.service.RegisterService;
import uk.gov.bis.lite.permissions.service.SubmissionService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("")
public class RegisterOgelResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterOgelResource.class);

  private static final String ERROR_ALREADY_IN_QUEUE = "ALREADY_IN_QUEUE";
  private static final String ERROR_INVALID_REQUEST = "INVALID_REQUEST";

  private final RegisterService registerService;
  private final SubmissionService submissionService;

  @Inject
  public RegisterOgelResource(RegisterService registerService, SubmissionService submissionService) {
    this.registerService = registerService;
    this.submissionService = submissionService;
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/register-ogel")
  public Response registerOgel(RegisterOgel registerOgel) {
    LOGGER.info("************ registerOgel ************ ");

    // Check if register Ogel request is valid
    if (!registerOgel.isValid()) {
      return badRequest(ERROR_INVALID_REQUEST, "Request invalid - " + registerOgel.getValidityInfo());
    }

    // Check if we are already processing this register Ogel request
    if (submissionService.submissionCurrentlyExists(registerOgel.generateSubmissionReference())) {
      return badRequest(ERROR_ALREADY_IN_QUEUE, "Duplicate request exists in the queue");
    }

    // Creates and persists an OgelSubmission
    String submissionRef = registerService.register(registerOgel);

    LOGGER.info("************ registerOgel : " + submissionRef);

    // Return with new submission reference
    return goodSubmissionRequest(submissionRef);
  }

  private Response badRequest(String errorCode, String message) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(ImmutableMap.of("code", Response.Status.BAD_REQUEST, "errorCode", errorCode, "errorMessage", message))
        .type(MediaType.APPLICATION_JSON_TYPE)
        .build();
  }

  private Response badRequest(String message) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(ImmutableMap.of("code", Response.Status.BAD_REQUEST, "message", message))
        .type(MediaType.APPLICATION_JSON_TYPE)
        .build();
  }

  private Response goodSubmissionRequest(String ref) {
    return Response.ok("{\"requestId\": \"" + ref + "\"}", MediaType.APPLICATION_JSON).build();
  }

  private Response goodRequest() {
    return Response.ok("{\"status\": \"success\"}", MediaType.APPLICATION_JSON).build();
  }
}

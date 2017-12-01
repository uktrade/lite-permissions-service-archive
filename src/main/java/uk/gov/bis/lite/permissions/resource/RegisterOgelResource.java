package uk.gov.bis.lite.permissions.resource;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.RegisterService;
import uk.gov.bis.lite.permissions.service.SubmissionService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/register-ogel")
  public Response registerOgel(RegisterParam registerParam, @QueryParam("callbackUrl") String callbackUrl, @Auth LiteJwtUser liteJwtUser) {
    LOGGER.info("************ register-ogel ************ ");

    // Check if RegisterParam request is valid
    if (!registerService.isRegisterParamValid(registerParam)) {
      return badRequest(ERROR_INVALID_REQUEST, "Request invalid - "
          + registerService.getRegisterParamValidationInfo(registerParam));
    }

    // Check if we are already processing RegisterParam request
    if (submissionService.submissionCurrentlyExists(registerService.generateSubmissionReference(registerParam))) {
      return badRequest(ERROR_ALREADY_IN_QUEUE, "Duplicate request exists in the queue");
    }

    // Creates and persists an OgelSubmission
    OgelSubmission sub = registerService.getOgelSubmission(registerParam, liteJwtUser);
    String requestId = registerService.register(sub, callbackUrl);

    LOGGER.info("************ register-ogel : {}", requestId);

    // Return with new requestId (submissionRef + submissionId)
    return goodSubmissionRequest(requestId);
  }

  private Response badRequest(String errorCode, String message) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(ImmutableMap.of("code", Response.Status.BAD_REQUEST, "errorCode", errorCode, "errorMessage", message))
        .type(MediaType.APPLICATION_JSON_TYPE)
        .build();
  }

  private Response goodSubmissionRequest(String ref) {
    return Response.ok("{\"requestId\": \"" + ref + "\"}", MediaType.APPLICATION_JSON).build();
  }
}

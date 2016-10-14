package uk.gov.bis.lite.permissions.resource;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.model.register.RegisterOgel;
import uk.gov.bis.lite.permissions.service.RegisterService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("")
public class RegisterOgelResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterOgelResource.class);
  private final RegisterService registerService;

  @Inject
  public RegisterOgelResource(RegisterService registerService) {
    this.registerService = registerService;
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/register-ogel")
  public Response registerOgel(RegisterOgel registerOgel) {
    LOGGER.info("************ registerOgel ************ ");

    // Check valid request
    if (!registerOgel.isValid()) {
      return badRequest("Request invalid - " + registerOgel.getValidityInfo());
    }

    boolean created = true;

    // Check for existing Customer and existing Site
    if (registerOgel.isExistingCustomerAndSite() || registerOgel.isExistingCustomer() || registerOgel.isExistingSite()) {
      // assume valid for now
      created = registerService.register(registerOgel);
    } else {
      created = registerService.register(registerOgel);
    }

    if(!created) {
      return badRequest("The register Ogel request is currently being processed");
    }
    return goodRequest();
  }

  private Response badRequest(String message) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(ImmutableMap.of("code", Response.Status.BAD_REQUEST, "message", message))
        .type(MediaType.APPLICATION_JSON_TYPE)
        .build();
  }

  private Response goodRequest() {
    return Response.ok("{\"status\": \"success\"}", MediaType.APPLICATION_JSON).build();
  }
}

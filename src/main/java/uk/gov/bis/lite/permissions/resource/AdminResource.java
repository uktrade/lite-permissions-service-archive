package uk.gov.bis.lite.permissions.resource;

import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.auth.basic.Roles;
import uk.gov.bis.lite.common.auth.basic.User;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/admin")
public class AdminResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdminResource.class);

  @RolesAllowed(Roles.SERVICE)
  @GET
  @Path("/check")
  public Response adminCheck(@Auth User user) {
    LOGGER.info("Admin check request received, responding with 200 OK");
    return Response.status(Response.Status.OK).build();
  }

}
package uk.gov.bis.lite.permissions.resource;

import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.permissions.service.model.Status;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ResourceUtil {

  private ResourceUtil() {
  }

  /**
   * Validates the given userId against the id stored in {@link LiteJwtUser}. Throws {@link WebApplicationException}
   * with {@link Response.Status#UNAUTHORIZED} when the pair do not match.
   *
   * @param userId the userId to validate
   * @param user   the {@link LiteJwtUser} principle to validate against
   */
  static void validateUserIdToJwt(String userId, LiteJwtUser user) {
    if (!StringUtils.equals(userId, user.getUserId())) {
      String message = String.format("userId %s does not match value supplied in token %s", userId, user.getUserId());
      throw new WebApplicationException(message, Response.Status.UNAUTHORIZED);
    }
  }

  /**
   * Validates the given service result {@link Status}, throws a {@link WebApplicationException} when not {@link Status#OK}
   *
   * @param status result status
   */
  static void validateServiceStatus(Status status, String errorMessage) {
    switch (status) {
      case OK:
        return;
      case USER_ID_NOT_FOUND:
      case REGISTRATION_NOT_FOUND:
        throw new WebApplicationException(errorMessage, Response.Status.NOT_FOUND);
      case TOO_MANY_REGISTRATIONS:
        throw new WebApplicationException(errorMessage, Response.Status.BAD_REQUEST);
      default:
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }
}

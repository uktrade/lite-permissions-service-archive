package uk.gov.bis.lite.permissions.resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.permissions.service.model.Status;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ResourceUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUtil.class);

  private ResourceUtil() {
  }

  /**
   * Validates the given userId against the id stored in {@link LiteJwtUser}. Throws {@link WebApplicationException} with
   *  {@link Response.Status#UNAUTHORIZED} when the pair do not match.
   * @param userId the userId to validate
   * @param user the {@Link LiteJwtUser} principle to validate against
   */
  static void validateUserIdToJwt(String userId, LiteJwtUser user) {
   if (!StringUtils.equals(userId, user.getUserId())) {
      throw new WebApplicationException("userId \"" + userId + "\" does not match value supplied in token (" +
          user.getUserId() + ")", Response.Status.UNAUTHORIZED);
    }
  }

  /**
   * Validates the given service result {@link Status}, throws a {@link WebApplicationException} when not {@link Status#OK}
   * @param status result status
   */
  static void validateServiceStatus(Status status) {
    switch (status) {
      case OK:
        return;
      case USER_ID_NOT_FOUND:
        throw new WebApplicationException("User not found.", Response.Status.NOT_FOUND);
      default:
        throw new WebApplicationException("Unexpected value for ServiceResult.Status", Response.Status.INTERNAL_SERVER_ERROR);
    }
  }
}

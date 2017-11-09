package uk.gov.bis.lite.permissions.resource;

import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ResourceUtil {
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
}

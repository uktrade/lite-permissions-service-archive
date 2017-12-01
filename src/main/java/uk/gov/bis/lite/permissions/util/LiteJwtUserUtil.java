package uk.gov.bis.lite.permissions.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;

import java.io.IOException;

public class LiteJwtUserUtil {
  private static Logger LOGGER = LoggerFactory.getLogger(LiteJwtUserUtil.class);

  LiteJwtUserUtil() {
  }

  public static String toJson(LiteJwtUser liteJwtUser) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(liteJwtUser);
    } catch (IOException e) {
      LOGGER.error("IOException", e);
      return null;
    }
  }

  public static LiteJwtUser fromJson(String liteJwtUserJson) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(liteJwtUserJson, LiteJwtUser.class);
    } catch (IOException e) {
      LOGGER.error("IOException", e);
      return null;
    }
  }
}

package uk.gov.bis.lite.permissions.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;

import java.io.IOException;

public class LiteJwtUserUtil {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static String toJson(LiteJwtUser liteJwtUser) {
    try {
      return MAPPER.writeValueAsString(liteJwtUser);
    } catch (IOException e) {
      throw new RuntimeException("Exception while attempting to serialize a liteJwtUser to json", e);
    }
  }

  public static LiteJwtUser fromJson(String liteJwtUserJson) {
    try {
      return MAPPER.readValue(liteJwtUserJson, LiteJwtUser.class);
    } catch (IOException e) {
      throw new RuntimeException("Exception while attempting to deserialize a liteJwtUser from json", e);
    }
  }
}

package uk.gov.bis.lite.permissions.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;
import uk.gov.bis.lite.common.jwt.LiteJwtUserHelper;

import java.io.IOException;

public class LiteJwtUserUtil {

  public static String toJson(LiteJwtUser liteJwtUser) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(liteJwtUser);
    } catch (IOException e) {
      throw new RuntimeException("Exception while attempting to serialize a liteJwtUser to json", e);
    }
  }

  public static LiteJwtUser fromJson(String liteJwtUserJson) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(liteJwtUserJson, LiteJwtUser.class);
    } catch (IOException e) {
      throw new RuntimeException("Exception while attempting to deserialize a liteJwtUser from json", e);
    }
  }
}

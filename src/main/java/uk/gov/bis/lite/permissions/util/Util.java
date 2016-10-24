package uk.gov.bis.lite.permissions.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import javax.ws.rs.core.Response;

public class Util {

  public static boolean isBlank(String arg) {
    return StringUtils.isBlank(arg);
  }

  public static String generateHashFromString(String message) {
    byte[] resultByte = DigestUtils.md5(message);
    return new String(Hex.encodeHex(resultByte));
  }

  public static String getInfo(Exception e) {
    String info = e.getClass().getCanonicalName();
    String message = e.getMessage();
    if(message != null) {
      info = info + " [" + message + "]";
    }
    return info;
  }

  public static String getInfo(Response response) {
    String info = "Response is null";
    if(response != null) {
      info = "Status [" + response.getStatus() + " |" + response.readEntity(String.class) + "]";
    }
    return info;
  }

  public static String info(String name, String arg) {
    return " [" + name + "|" + Optional.ofNullable(arg).map(Object::toString).orElse("null") + "] ";
  }

  public static String info(String name, Boolean arg) {
    return " [" + name + "|" + Optional.ofNullable(arg).map(Object::toString).orElse("null") + "] ";
  }

  public static String joinDelimited(String delimited, String... args) {
    return StringUtils.join(args, delimited);
  }

  public static String joinAll(String... args) {
    return StringUtils.join(args);
  }

  public static String joinAll(boolean... args) {
    String all = "";
    for (Boolean arg : args) {
      all = all + arg.toString();
    }
    return all;
  }
}

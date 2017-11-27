package uk.gov.bis.lite.permissions.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import javax.ws.rs.core.Response;

public class Util {

  public static String generateHashFromString(String message) {
    byte[] resultByte = DigestUtils.md5(message);
    return new String(Hex.encodeHex(resultByte));
  }

  /**
   * Returns exception class name and message as String
   */
  public static String info(Exception e) {
    String info = e.getClass().getCanonicalName();
    String message = e.getMessage();
    if (message != null) {
      info = info + " [" + message + "]";
    }
    return info;
  }

  /**
   * Returns response status and body as String
   */
  public static String info(Response response) {
    String status = "-";
    String body = "-";
    if (response != null) {
      status = "" + response.getStatus();
      if (response.hasEntity()) {
        body = response.readEntity(String.class);
      }
    }
    return "Status[" + status + "] Body[" + body + "]";
  }

}

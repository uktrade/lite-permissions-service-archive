package uk.gov.bis.lite.permissions.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class Util {

  public static String info(String name, String arg) {
    return " [" + name + "|" + Optional.ofNullable(arg).map(Object::toString).orElse("null") + "] ";
  }

  public static String info(String name, Boolean arg) {
    return " [" + name + "|" + Optional.ofNullable(arg).map(Object::toString).orElse("null") + "] ";
  }

  public static boolean allNotBlank(String... args) {
    boolean allNotBlank = true;
    for (String arg : args) {
      if (StringUtils.isBlank(arg)) {
        allNotBlank = false;
        break;
      }
    }
    return allNotBlank;
  }

  public static String getOptString(String info, boolean add) {
    String result = "";
    if (add) {
      result = info;
    }
    return result;
  }
}

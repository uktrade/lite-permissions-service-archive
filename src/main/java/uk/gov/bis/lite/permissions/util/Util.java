package uk.gov.bis.lite.permissions.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class Util {

  public static boolean isBlank(String arg) {
    return StringUtils.isBlank(arg);
  }

  public static boolean getRandomBoolean() {
    return Math.random() < 0.5;
  }

  public static String generateHashFromString(String message) {
    String hash = "hash";
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
      byte[] array = md.digest(message.getBytes());
      StringBuffer sb = new StringBuffer();
      for (byte anArray : array) {
        sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
      }
      hash = sb.toString();
    } catch (java.security.NoSuchAlgorithmException e) {
      // ignore
    }
    return hash;
  }

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

  public static String joinAll(String... args) {
    String all = "";
    for (String arg : args) {
      if (!StringUtils.isBlank(arg)) {
        all = all + arg;
      }
    }
    return all;
  }

  public static String joinAll(boolean... args) {
    String all = "";
    for (Boolean arg : args) {
      all = all + arg.toString();
    }
    return all;
  }

  public static String getOptString(String info, boolean add) {
    String result = "";
    if (add) {
      result = info;
    }
    return result;
  }
}

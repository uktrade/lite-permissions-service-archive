package uk.gov.bis.lite.permissions.api.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public enum Status {
  ACTIVE,
  SURRENDERED,
  REVOKED,
  EXPIRED,
  EXHAUSTED;

  @JsonCreator
  public static Status fromJsonValue(String value) {
    return getEnumByValue(value).orElse(null);
  }

  @JsonValue
  public String getValue() {
    return this.name();
  }

  public static Optional<Status> getEnumByValue(String value) {
    return Arrays.stream(Status.values())
        .filter(e -> StringUtils.equals(value, e.getValue()))
        .findAny();
  }
}

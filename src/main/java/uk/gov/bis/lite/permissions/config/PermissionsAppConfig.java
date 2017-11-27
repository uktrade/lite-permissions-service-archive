package uk.gov.bis.lite.permissions.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PermissionsAppConfig extends Configuration {

  @NotEmpty
  private String processScheduledJobCron;

  @NotEmpty
  @JsonProperty
  private String spireClientUserName;

  @NotEmpty
  @JsonProperty
  private String spireClientPassword;

  @NotEmpty
  @JsonProperty
  private String spireClientUrl;

  @NotEmpty
  private String customerServiceUrl;

  @NotEmpty
  private String maxMinutesRetryAfterFail;

  @NotEmpty
  private String maxCallbackFailCount;

  @Valid
  @NotNull
  @JsonProperty("database")
  private DataSourceFactory database;

  @NotEmpty
  private String adminLogin;

  @NotEmpty
  private String adminPassword;

  @NotEmpty
  private String jwtSharedSecret;

  public DataSourceFactory getDataSourceFactory() {
    return database;
  }

  public String getProcessScheduledJobCron() {
    return processScheduledJobCron;
  }

  public String getSpireClientUserName() {
    return spireClientUserName;
  }

  public String getSpireClientPassword() {
    return spireClientPassword;
  }

  public String getSpireClientUrl() {
    return spireClientUrl;
  }

  public String getCustomerServiceUrl() {
    return customerServiceUrl;
  }

  public String getMaxMinutesRetryAfterFail() {
    return maxMinutesRetryAfterFail;
  }

  public DataSourceFactory getDatabase() {
    return database;
  }

  public String getAdminLogin() {
    return adminLogin;
  }

  public String getAdminPassword() {
    return adminPassword;
  }

  public String getMaxCallbackFailCount() {
    return maxCallbackFailCount;
  }

  public String getJwtSharedSecret() {
    return jwtSharedSecret;
  }
}

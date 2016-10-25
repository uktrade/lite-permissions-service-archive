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
  private String customerServiceUrl;

  @NotEmpty
  private String customerServiceCustomerPath;

  @NotEmpty
  private String customerServiceSitePath;

  @NotEmpty
  private String customerServiceUserRolePath;

  @NotEmpty
  @JsonProperty
  private String spireServiceUserName;

  @NotEmpty
  @JsonProperty
  private String spireServicePassword;

  @NotEmpty
  @JsonProperty
  private String spireServiceUrl;

  @NotEmpty
  @JsonProperty
  private String spireServiceActiveEndpoints;

  @NotEmpty
  private String maxMinutesRetryAfterFail;

  @Valid
  @NotNull
  @JsonProperty("database")
  private DataSourceFactory database = new DataSourceFactory();

  @NotEmpty
  private String adminLogin;

  @NotEmpty
  private String adminPassword;

  public DataSourceFactory getDataSourceFactory() {
    return database;
  }

  public String getProcessScheduledJobCron() {
    return processScheduledJobCron;
  }

  public String getCustomerServiceUrl() {
    return customerServiceUrl;
  }

  public String getCustomerServiceCustomerPath() {
    return customerServiceCustomerPath;
  }

  public String getCustomerServiceSitePath() {
    return customerServiceSitePath;
  }

  public String getCustomerServiceUserRolePath() {
    return customerServiceUserRolePath;
  }

  public String getSpireServiceUserName() {
    return spireServiceUserName;
  }

  public String getSpireServicePassword() {
    return spireServicePassword;
  }

  public String getSpireServiceUrl() {
    return spireServiceUrl;
  }

  public String getSpireServiceActiveEndpoints() {
    return spireServiceActiveEndpoints;
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
}

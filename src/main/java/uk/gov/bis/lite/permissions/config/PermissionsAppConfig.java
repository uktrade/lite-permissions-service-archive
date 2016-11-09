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
  private String customerServiceCustomerPath;

  @NotEmpty
  private String customerServiceCustomerNumberPath;

  @NotEmpty
  private String customerServiceSitePath;

  @NotEmpty
  private String customerServiceUserRolePath;

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

  public String getCustomerServiceCustomerNumberPath() {
    return customerServiceCustomerNumberPath;
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

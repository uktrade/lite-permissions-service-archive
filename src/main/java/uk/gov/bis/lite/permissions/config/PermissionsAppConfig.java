package uk.gov.bis.lite.permissions.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PermissionsAppConfig extends Configuration {

  @NotEmpty
  private String customerServiceUrl;

  @NotEmpty
  private String customerServiceCustomerPath;

  @NotEmpty
  private String customerServiceSitePath;

  @NotEmpty
  private String customerServiceUserRolePath;

  @NotEmpty
  private String maxFailRetryWindowMinutes;

  @NotEmpty
  @JsonProperty
  private String spireCreateOgelAppUrl;

  @NotEmpty
  @JsonProperty
  private String soapUserName;

  @NotEmpty
  @JsonProperty
  private String soapPassword;

  @NotEmpty
  private String processScheduledJobCron;

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

  public String getSpireCreateOgelAppUrl() {
    return spireCreateOgelAppUrl;
  }

  public String getSoapUserName() {
    return soapUserName;
  }

  public String getSoapPassword() {
    return soapPassword;
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

  public String getMaxFailRetryWindowMinutes() {
    return maxFailRetryWindowMinutes;
  }

  public String getProcessScheduledJobCron() {
    return processScheduledJobCron;
  }
}

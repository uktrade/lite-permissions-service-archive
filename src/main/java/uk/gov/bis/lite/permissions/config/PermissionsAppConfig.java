package uk.gov.bis.lite.permissions.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PermissionsAppConfig extends Configuration {

  @Valid
  @NotNull
  private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

  @JsonProperty("jerseyClient")
  public JerseyClientConfiguration getJerseyClientConfiguration() {
    return jerseyClient;
  }

  @NotEmpty
  private String customerServiceHost;

  @NotEmpty
  private String customerServicePort;

  @NotEmpty
  private String customerServiceCustomerPath;

  @NotEmpty
  private String customerServiceSitePath;

  @NotEmpty
  @JsonProperty
  private String spireCreateLiteSarUrl;

  @NotEmpty
  @JsonProperty
  private String spireOgelRegistrationsUrl;

  @NotEmpty
  @JsonProperty
  private String soapUserName;

  @NotEmpty
  @JsonProperty
  private String soapPassword;

  @NotEmpty
  private String notificationRetryJobCron;

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

  public String getSpireCreateLiteSarUrl() {
    return spireCreateLiteSarUrl;
  }

  public void setSpireCreateLiteSarUrl(String spireCreateLiteSarUrl) {
    this.spireCreateLiteSarUrl = spireCreateLiteSarUrl;
  }

  public String getSpireOgelRegistrationsUrl() {
    return spireOgelRegistrationsUrl;
  }

  public String getSoapUserName() {
    return soapUserName;
  }

  public String getSoapPassword() {
    return soapPassword;
  }

  public String getNotificationRetryJobCron() {
    return notificationRetryJobCron;
  }

  public String getAdminLogin() {
    return adminLogin;
  }

  public String getAdminPassword() {
    return adminPassword;
  }

  public String getCustomerServiceHost() {
    return customerServiceHost;
  }

  public void setCustomerServiceHost(String customerServiceHost) {
    this.customerServiceHost = customerServiceHost;
  }

  public String getCustomerServicePort() {
    return customerServicePort;
  }

  public void setCustomerServicePort(String customerServicePort) {
    this.customerServicePort = customerServicePort;
  }


  public String getCustomerServiceCustomerPath() {
    return customerServiceCustomerPath;
  }

  public void setCustomerServiceCustomerPath(String customerServiceCustomerPath) {
    this.customerServiceCustomerPath = customerServiceCustomerPath;
  }

  public String getCustomerServiceSitePath() {
    return customerServiceSitePath;
  }

  public void setCustomerServiceSitePath(String customerServiceSitePath) {
    this.customerServiceSitePath = customerServiceSitePath;
  }
}

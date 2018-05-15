package uk.gov.bis.lite.permissions.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.bis.lite.common.paas.db.SchemaAwareDataSourceFactory;

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
  private SchemaAwareDataSourceFactory dataSourceFactory;

  @NotEmpty
  private String adminLogin;

  @NotEmpty
  private String adminPassword;

  @NotEmpty
  private String jwtSharedSecret;

  @NotEmpty
  private String login;

  @NotEmpty
  private String password;

  public SchemaAwareDataSourceFactory getDataSourceFactory() {
    return dataSourceFactory;
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

  public String getLogin() {
    return login;
  }

  public String getPassword() {
    return password;
  }

}

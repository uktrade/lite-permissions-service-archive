package uk.gov.bis.lite.permissions;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import uk.gov.bis.lite.permissions.config.GuiceModule;
import uk.gov.bis.lite.permissions.config.PactDaoModule;
import uk.gov.bis.lite.permissions.config.PactServiceModule;
import uk.gov.bis.lite.permissions.config.PermissionsAppConfig;
import uk.gov.bis.lite.permissions.config.TestServiceModule;

public class PactPermissionsApp extends PermissionsApp {

  public PactPermissionsApp() {
    super(new Module[]{new GuiceModule(),
        Modules.override(new TestServiceModule()).with(new PactServiceModule()),
        new PactDaoModule()});
  }

  @Override
  protected void flywayMigrate(PermissionsAppConfig config) {

  }
}

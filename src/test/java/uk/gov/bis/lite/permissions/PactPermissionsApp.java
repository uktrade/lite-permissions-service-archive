package uk.gov.bis.lite.permissions;

import com.google.inject.util.Modules;
import uk.gov.bis.lite.permissions.config.GuiceModule;

public class PactPermissionsApp extends PermissionsApp {

  public PactPermissionsApp() {
    super(Modules.override(new GuiceModule()).with(new PactGuiceModule()));
  }

}

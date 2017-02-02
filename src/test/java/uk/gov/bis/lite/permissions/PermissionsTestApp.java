package uk.gov.bis.lite.permissions;

import com.google.inject.util.Modules;
import uk.gov.bis.lite.permissions.config.GuiceModule;

public class PermissionsTestApp extends PermissionsApp {

  public PermissionsTestApp() {
    super(Modules.override(new GuiceModule()).with(new GuiceTestModule()));
  }

  public <T> T getInstance(Class<T> type) {
    return getGuiceBundle().getInjector().getInstance(type);
  }

}

package uk.gov.bis.lite.permissions;

import com.google.inject.Module;
import uk.gov.bis.lite.permissions.config.DaoModule;
import uk.gov.bis.lite.permissions.config.GuiceModule;
import uk.gov.bis.lite.permissions.config.TestServiceModule;

public class TestPermissionsApp extends PermissionsApp {

  public TestPermissionsApp() {
    super(new Module[]{new GuiceModule(), new TestServiceModule(), new DaoModule()});
  }

}

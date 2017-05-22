package uk.gov.bis.lite.permissions;

import com.google.inject.util.Modules;
import uk.gov.bis.lite.permissions.config.GuiceModule;

/**
 * Use for integration tests - see ProcessSubmissionServiceTest
 */
public class TestPermissionsApp extends PermissionsApp {

  public TestPermissionsApp() {
    super(Modules.override(new GuiceModule()).with(new TestGuiceModule()));
  }

}
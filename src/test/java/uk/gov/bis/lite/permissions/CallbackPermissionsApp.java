package uk.gov.bis.lite.permissions;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import uk.gov.bis.lite.permissions.config.CallbackServiceModule;
import uk.gov.bis.lite.permissions.config.DaoModule;
import uk.gov.bis.lite.permissions.config.GuiceModule;
import uk.gov.bis.lite.permissions.config.TestServiceModule;

/**
 * Use for integration tests - see ProcessSubmissionServiceTest
 */
public class CallbackPermissionsApp extends PermissionsApp {

  public CallbackPermissionsApp() {
    super(new Module[]{new GuiceModule(),
        Modules.override(new TestServiceModule()).with(new CallbackServiceModule()),
        new DaoModule()});
  }

}

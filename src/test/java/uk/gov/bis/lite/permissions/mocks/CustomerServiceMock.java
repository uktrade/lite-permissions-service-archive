package uk.gov.bis.lite.permissions.mocks;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.CustomerService;

import java.util.Optional;

@Singleton
public class CustomerServiceMock implements CustomerService {

  private boolean createCustomerSuccess = true;
  private boolean createSiteSuccess = true;
  private boolean updateUserRoleSuccess = true;

  @Override
  public Optional<String> getOrCreateCustomer(OgelSubmission sub) {
    return createCustomerSuccess ? Optional.of("MOCK_SAR") : Optional.empty();
  }

  @Override
  public Optional<String> createSite(OgelSubmission sub) {
    return createSiteSuccess ? Optional.of("MOCK_SITE") : Optional.empty();
  }

  @Override
  public boolean updateUserRole(OgelSubmission sub) {
    return updateUserRoleSuccess;
  }

  public void setAllSuccess(boolean arg) {
    setCreateCustomerSuccess(arg);
    setCreateSiteSuccess(arg);
    setUpdateUserRoleSuccess(arg);
  }

  public void setCreateCustomerSuccess(boolean createCustomerSuccess) {
    this.createCustomerSuccess = createCustomerSuccess;
  }

  public void setCreateSiteSuccess(boolean createSiteSuccess) {
    this.createSiteSuccess = createSiteSuccess;
  }

  public void setUpdateUserRoleSuccess(boolean updateUserRoleSuccess) {
    this.updateUserRoleSuccess = updateUserRoleSuccess;
  }
}

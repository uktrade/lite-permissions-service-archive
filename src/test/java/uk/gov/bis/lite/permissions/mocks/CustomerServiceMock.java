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

  private int customerCallCount = 0;
  private int siteCallCount = 0;
  private int uerRoleCallCount = 0;

  @Override
  public Optional<String> getOrCreateCustomer(OgelSubmission sub) {
    customerCallCount++;
    return createCustomerSuccess ? Optional.of("MOCK_SAR") : Optional.empty();
  }

  @Override
  public Optional<String> createSite(OgelSubmission sub) {
    siteCallCount++;
    return createSiteSuccess ? Optional.of("MOCK_SITE") : Optional.empty();
  }

  @Override
  public boolean updateUserRole(OgelSubmission sub) {
    uerRoleCallCount++;
    return updateUserRoleSuccess;
  }

  public void setAllSuccess(boolean arg) {
    setCreateCustomerSuccess(arg);
    setCreateSiteSuccess(arg);
    setUpdateUserRoleSuccess(arg);
  }

  public void resetAllCounts() {
    resetCustomerCallCount();
    resetSiteCallCount();
    resetUserRoleCallCount();
  }

  public void resetCustomerCallCount() {
    this.customerCallCount = 0;
  }

  public void resetSiteCallCount() {
    this.siteCallCount = 0;
  }

  public void resetUserRoleCallCount() {
    this.uerRoleCallCount = 0;
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

  public int getCustomerCallCount() {
    return customerCallCount;
  }

  public int getSiteCallCount() {
    return siteCallCount;
  }

  public int getUserRoleCallCount() {
    return uerRoleCallCount;
  }
}

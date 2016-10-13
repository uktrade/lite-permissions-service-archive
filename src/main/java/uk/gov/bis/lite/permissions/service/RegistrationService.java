package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelRegistrationDao;
import uk.gov.bis.lite.permissions.model.OgelRegistration;

import java.util.List;
import java.util.Optional;

@Singleton
public class RegistrationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class);

  private SoapService soap;
  private OgelRegistrationDao dao;
  private CustomerService customerService;

  private static final String USER_ROLE_UPDATE_STATUS_COMPLETE = "COMPLETE";
  private static final String USER_ROLE_UPDATE_STATUS_ERROR = "Error";

  @Inject
  public RegistrationService(SoapService soap, OgelRegistrationDao dao, CustomerService customerService) {
    this.soap = soap;
    this.dao = dao;
    this.customerService = customerService;
  }

  public void processRegistrations(OgelRegistration.Status status) {
    if(status.equals(OgelRegistration.Status.PENDING)) {
      doPendingRegistrations();
    }
    if(status.equals(OgelRegistration.Status.CUSTOMER)) {
      doCreateCustomers();
    }
    if(status.equals(OgelRegistration.Status.SITE)) {
      doCreateSites();
    }
    if(status.equals(OgelRegistration.Status.USER_ROLE)) {
      doUserRolePermissionUpdates();
    }
  }

  private void doPendingRegistrations() {
    LOGGER.info("Found PENDING [" + dao.getByStatus(OgelRegistration.Status.PENDING.name()).size() + "]");
  }

  /**
   * For each OgelRegistration we need to create a Customer on Spire
   * and then update the OgelRegistration status
   */
  private void doCreateCustomers() {
    List<OgelRegistration> regs = dao.getByStatus(OgelRegistration.Status.CUSTOMER.name());
    LOGGER.info("Found CUSTOMER [" + regs.size() + "]");
    for(OgelRegistration reg : regs) {
      Optional<String> sarRef = customerService.createCustomer(reg);
      if(sarRef.isPresent()) {
        reg.setCustomerId(sarRef.get());
        reg.updateStatus();
        dao.update(reg);
        LOGGER.info("Customer created. Updated record: " + sarRef.get());
      }
    }
  }

  /**
   * For each OgelRegistration we need to create a Site on Spire
   * and then update the OgelRegistration status
   */
  private void doCreateSites() {
    List<OgelRegistration> regs = dao.getByStatus(OgelRegistration.Status.SITE.name());
    LOGGER.info("Found SITE [" + regs.size() + "]");
    for(OgelRegistration reg : regs) {
      Optional<String> siteRef = customerService.createSite(reg);
      if(siteRef.isPresent()) {
        reg.setSiteId(siteRef.get());
        reg.updateStatus();
        dao.update(reg);
        LOGGER.info("Site created. Updated record: " + siteRef.get());
      }
    }
  }

  /**
   * For each OgelRegistration we update site access permissions and
   * then set the OgelRegistration status to READY
   */
  private void doUserRolePermissionUpdates() {
    List<OgelRegistration> regs = dao.getByStatus(OgelRegistration.Status.USER_ROLE.name());
    LOGGER.info("Found USER_ROLE [" + regs.size() + "]");
    for(OgelRegistration reg : regs) {
      Optional<String> status = customerService.updateUserRole(reg);
      if(status.isPresent() && status.get().equals(USER_ROLE_UPDATE_STATUS_COMPLETE)) {
        reg.updateStatusToReady();
        dao.update(reg);
        LOGGER.info("USer role permission updated. Updated OgelRegistration to READY: " + reg.getUserId() + "/" + reg.getOgelType());
      }
    }
  }

}

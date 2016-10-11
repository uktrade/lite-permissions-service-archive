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
      doCustomerRegistrations();
    }
    if(status.equals(OgelRegistration.Status.SITE)) {
      doSiteRegistrations();
    }
    if(status.equals(OgelRegistration.Status.SITE_PERMISSION)) {
      doPermissionRegistrations();
    }
  }

  private void doPendingRegistrations() {
    LOGGER.info("doPendingRegistrations: " + dao.getByStatus(OgelRegistration.Status.PENDING.name()).size());
  }

  /**
   * For each OgelRegistration we need to create a Customer on Spire
   * and then update the OgelRegistration status
   */
  private void doCustomerRegistrations() {
    List<OgelRegistration> regs = dao.getByStatus(OgelRegistration.Status.CUSTOMER.name());
    LOGGER.info("doCustomerRegistrations: " + regs.size());
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
  private void doSiteRegistrations() {
    List<OgelRegistration> regs = dao.getByStatus(OgelRegistration.Status.SITE.name());
    LOGGER.info("doSiteRegistrations: " + regs.size());
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

  private void doPermissionRegistrations() {
    LOGGER.info("doPermissionRegistrations: " + dao.getByStatus(OgelRegistration.Status.SITE_PERMISSION.name()).size());
  }

}

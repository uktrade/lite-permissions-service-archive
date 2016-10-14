package uk.gov.bis.lite.permissions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.model.register.RegisterOgel;

@Singleton
public class RegisterService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterService.class);

  private OgelSubmissionDao submissionDao;

  @Inject
  public RegisterService(OgelSubmissionDao submissionDao) {
    this.submissionDao = submissionDao;
  }

  public boolean register(RegisterOgel reg) {
    LOGGER.info("Creating OgelSubmission: " + reg.getUserId() + "/" + reg.getOgelType());
    OgelSubmission sub = getOgelRegistration(reg);
    sub.setInitialStatus();

    // Check if we already have this request
    if (submissionDao.findBySubmissionRef(sub.getSubmissionRef()) != null) {
      LOGGER.info("OgelSubmission request already exists, current status: " + sub.getStatus().name());
      return false;
    }

    submissionDao.create(sub);
    return true;
  }

  private OgelSubmission getOgelRegistration(RegisterOgel reg) {
    ObjectMapper mapper = new ObjectMapper();
    OgelSubmission sub = new OgelSubmission(reg.getUserId(), reg.getOgelType());
    sub.setCustomerRef(reg.getExistingCustomer());
    sub.setSiteRef(reg.getExistingSite());
    sub.setSubmissionRef(reg.generateSubmissionReference());
    sub.setRoleUpdate(reg.isRoleUpdateRequired());
    try {
      sub.setJson(mapper.writeValueAsString(reg).replaceAll("\\s{2,}", " ").trim()); // remove excessive whitespace
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return sub;
  }
}

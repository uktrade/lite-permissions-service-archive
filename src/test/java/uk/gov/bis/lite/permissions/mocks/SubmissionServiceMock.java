package uk.gov.bis.lite.permissions.mocks;

import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.SubmissionService;

public class SubmissionServiceMock implements SubmissionService {

  private boolean submissionCurrentlyExists = false;

  public boolean submissionCurrentlyExists(String subRef) {
    return submissionCurrentlyExists;
  }

  public boolean processForCustomer(OgelSubmission sub) {
    return true;
  }

  public boolean processForSite(OgelSubmission sub) {
    return true;
  }

  public boolean processForRoleUpdate(OgelSubmission sub) {
    return true;
  }

  public void setSubmissionCurrentlyExists(boolean submissionCurrentlyExists) {
    this.submissionCurrentlyExists = submissionCurrentlyExists;
  }
}

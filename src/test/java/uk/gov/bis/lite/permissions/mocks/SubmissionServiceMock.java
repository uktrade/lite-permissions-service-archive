package uk.gov.bis.lite.permissions.mocks;

import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.SubmissionService;

public class SubmissionServiceMock implements SubmissionService {

  private boolean submissionCurrentlyExists = false;

  public boolean submissionCurrentlyExists(String subRef) {
    return submissionCurrentlyExists;
  }

  public boolean prepareCustomer(OgelSubmission sub) {
    return true;
  }

  public boolean prepareSite(OgelSubmission sub) {
    return true;
  }

  public boolean prepareRoleUpdate(OgelSubmission sub) {
    return true;
  }

  public void updateModeIfNotCompleted(int submissionId) {

  }

  public void setSubmissionCurrentlyExists(boolean submissionCurrentlyExists) {
    this.submissionCurrentlyExists = submissionCurrentlyExists;
  }
}

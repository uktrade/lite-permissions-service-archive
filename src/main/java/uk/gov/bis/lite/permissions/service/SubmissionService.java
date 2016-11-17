package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.model.OgelSubmission;

public interface SubmissionService {

  boolean submissionCurrentlyExists(String subRef);

  boolean prepareCustomer(OgelSubmission sub);

  boolean prepareSite(OgelSubmission sub);

  boolean prepareRoleUpdate(OgelSubmission sub);

  void updateModeIfNotCompleted(int submissionId);

}

package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.model.OgelSubmission;

public interface SubmissionService {

  boolean submissionCurrentlyExists(String subRef);

  boolean processForCustomer(OgelSubmission sub);

  boolean processForSite(OgelSubmission sub);

  boolean processForRoleUpdate(OgelSubmission sub);

}

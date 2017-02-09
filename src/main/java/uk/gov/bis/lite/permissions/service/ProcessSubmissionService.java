package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.model.OgelSubmission;

public interface ProcessSubmissionService {

  void processImmediate(int submissionId);

  void processOgelSubmissions();

  void doProcessOgelSubmission(OgelSubmission sub);

  OgelSubmission.Stage progressStage(OgelSubmission sub);

}

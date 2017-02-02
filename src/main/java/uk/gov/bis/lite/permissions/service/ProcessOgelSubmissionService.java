package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.model.OgelSubmission;

public interface ProcessOgelSubmissionService {

  void processImmediate(int submissionId);

  void processOgelSubmissions();

  void doProcessOgelSubmission(OgelSubmission sub);

  void progressStage(OgelSubmission sub);

}

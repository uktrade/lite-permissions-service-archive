package uk.gov.bis.lite.permissions.mocks;

import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.ProcessSubmissionService;

public class ProcessSubmissionServiceMock implements ProcessSubmissionService {

  @Override
  public void processImmediate(int submissionId) {

  }

  @Override
  public void processOgelSubmissions() {

  }

  @Override
  public void doProcessOgelSubmission(OgelSubmission sub) {

  }

  @Override
  public OgelSubmission.Stage progressStage(OgelSubmission sub) {
    return null;
  }
}

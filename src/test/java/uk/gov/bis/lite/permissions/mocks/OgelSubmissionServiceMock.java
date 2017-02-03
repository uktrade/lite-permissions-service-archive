package uk.gov.bis.lite.permissions.mocks;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.api.view.OgelSubmissionView;
import uk.gov.bis.lite.permissions.service.OgelSubmissionService;

import java.util.List;

@Singleton
public class OgelSubmissionServiceMock implements OgelSubmissionService {

  private boolean submissionCurrentlyExists = false;

  @Override
  public boolean ogelSubmissionExists(Integer submissionId) {
    return false;
  }

  public boolean submissionCurrentlyExists(String subRef) {
    return submissionCurrentlyExists;
  }

  @Override
  public List<OgelSubmissionView> getOgelSubmissions(String filter) {
    return null;
  }

  @Override
  public OgelSubmissionView getOgelSubmission(int submissionId) {
    return null;
  }

  @Override
  public void cancelPendingScheduledOgelSubmissions() {

  }

  @Override
  public void cancelScheduledOgelSubmission(int submissionId) {

  }

  public void setSubmissionCurrentlyExists(boolean submissionCurrentlyExists) {
    this.submissionCurrentlyExists = submissionCurrentlyExists;
  }
}

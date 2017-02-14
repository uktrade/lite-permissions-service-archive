package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.api.view.OgelSubmissionView;

import java.util.List;

public interface SubmissionService {

  boolean ogelSubmissionExists(Integer submissionId);

  boolean submissionCurrentlyExists(String subRef);

  List<OgelSubmissionView> getOgelSubmissions(String filter);

  OgelSubmissionView getOgelSubmission(int submissionId);

  void cancelPendingScheduledOgelSubmissions();

  void cancelScheduledOgelSubmission(int submissionId);

}

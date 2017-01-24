package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.api.view.OgelSubmissionView;

import java.util.List;

public interface OgelSubmissionService {

  boolean ogelSubmissionExists(Integer submissionId);

  List<OgelSubmissionView> getPendingScheduledOgelSubmissions();

  OgelSubmissionView getOgelSubmission(int submissionId);

  void cancelPendingScheduledOgelSubmissions();

  void cancelScheduledOgelSubmission(int submissionId);

}

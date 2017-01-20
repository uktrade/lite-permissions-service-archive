package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.api.view.OgelSubmissionView;

import java.util.List;

public interface OgelSubmissionService {

  List<OgelSubmissionView> getOgelSubmissions();

  OgelSubmissionView getOgelSubmission(int submissionId);

  void cancelScheduledOgelSubmissions();

  void cancelScheduledOgelSubmission(int submissionId);

}

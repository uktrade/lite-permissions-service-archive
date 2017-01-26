package uk.gov.bis.lite.permissions.dao;

import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.List;

public interface OgelSubmissionDao {

  void update(OgelSubmission sub);

  int create(OgelSubmission sub);

  List<OgelSubmission> getScheduledToProcess();

  List<OgelSubmission> getScheduledByStatus(OgelSubmission.Status status);

  List<OgelSubmission> getPendingSubmissions();

  List<OgelSubmission> getCancelledSubmissions();

  List<OgelSubmission> getCompleteSubmissions();

  OgelSubmission findBySubmissionRef(String submissionRef);

  OgelSubmission findBySubmissionId(int submissionId);

  OgelSubmission findRecentBySubmissionRef(String submissionRef);
}

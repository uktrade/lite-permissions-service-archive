package uk.gov.bis.lite.permissions.dao;

import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.List;

public interface OgelSubmissionDao {

  void update(OgelSubmission sub);

  void create(OgelSubmission sub);

  List<OgelSubmission> getScheduledByStatus(String status);

  OgelSubmission findBySubmissionRef(String submissionRef);

  OgelSubmission findRecentBySubmissionRef(String submissionRef);
}

package uk.gov.bis.lite.permissions.mocks;

import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.List;

public class OgelSubmissionDaoMock implements OgelSubmissionDao {

  @Override
  public void update(OgelSubmission sub) {

  }

  @Override
  public int create(OgelSubmission sub) {
    return 0;
  }

  @Override
  public List<OgelSubmission> getScheduledActive() {
    return null;
  }

  @Override
  public List<OgelSubmission> getScheduledCompleteToCallback() {
    return null;
  }

  @Override
  public List<OgelSubmission> getPendingSubmissions() {
    return null;
  }

  @Override
  public List<OgelSubmission> getCancelledSubmissions() {
    return null;
  }

  @Override
  public List<OgelSubmission> getFinishedSubmissions() {
    return null;
  }

  @Override
  public OgelSubmission findBySubmissionId(int submissionId) {
    return null;
  }

  @Override
  public OgelSubmission findRecentBySubmissionRef(String submissionRef) {
    return null;
  }
}

package uk.gov.bis.lite.permissions.mocks;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.List;

@Singleton
public class OgelSubmissionDaoMock implements OgelSubmissionDao {

  @Override
  public void update(OgelSubmission sub) {

  }

  @Override
  public long create(OgelSubmission sub) {
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
  public OgelSubmission findBySubmissionId(long submissionId) {
    return null;
  }

  @Override
  public OgelSubmission findRecentBySubmissionRef(String submissionRef) {
    return null;
  }

  @Override
  public OgelSubmission findBySubmissionRef(String submissionRef) {
    return null;
  }
}

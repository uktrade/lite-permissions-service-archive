package uk.gov.bis.lite.permissions.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.Transaction;
import uk.gov.bis.lite.permissions.dao.sqlite.OgelSubmissionInterface;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.List;

@Singleton
public class OgelSubmissionDaoImpl implements OgelSubmissionDao {

  private final DBI jdbi;

  @Inject
  public OgelSubmissionDaoImpl(@Named("jdbi") DBI jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  @Transaction
  public OgelSubmission findBySubmissionRef(String submissionRef) {
    try (final Handle handle = jdbi.open()) {
      return attach(handle).findBySubmissionRef(submissionRef);
    }
  }

  @Override
  @Transaction
  public OgelSubmission findBySubmissionId(int submissionId) {
    try (final Handle handle = jdbi.open()) {
      return attach(handle).findBySubmissionId(submissionId);
    }
  }

  @Override
  @Transaction
  public OgelSubmission findRecentBySubmissionRef(String submissionRef) {
    try (final Handle handle = jdbi.open()) {
      return attach(handle).findRecentBySubmissionRef(submissionRef);
    }
  }

  @Override
  @Transaction
  public List<OgelSubmission> getScheduled() {
    try (final Handle handle = jdbi.open()) {
      return attach(handle).getScheduled();
    }
  }

  @Override
  @Transaction
  public List<OgelSubmission> getScheduledByStatus(OgelSubmission.Status status) {
    try (final Handle handle = jdbi.open()) {
      return attach(handle).getScheduledByStatus(status.name());
    }
  }

  @Override
  @Transaction
  public List<OgelSubmission> getPendingScheduledSubmissions() {
    try (final Handle handle = jdbi.open()) {
      return attach(handle).getPendingScheduledSubmissions();
    }
  }

  @Override
  @Transaction
  public int create(OgelSubmission sub) {
    try (final Handle handle = jdbi.open()) {
      OgelSubmissionInterface subInterface = handle.attach(OgelSubmissionInterface.class);
      return subInterface.insert(
          sub.getUserId(),
          sub.getOgelType(),
          sub.getSubmissionRef(),
          sub.getCustomerRef(),
          sub.getSiteRef(),
          sub.getSpireRef(),
          sub.getCallbackUrl(),
          sub.isCalledBack(),
          sub.getJson(),
          sub.getMode().name(),
          sub.getStatus().name(),
          sub.isRoleUpdate(),
          sub.isRoleUpdated());
    }
  }

  @Override
  @Transaction
  public void update(OgelSubmission sub) {
    try (final Handle handle = jdbi.open()) {
      String failReason = null;
      if (sub.getFailReason() != null) {
        failReason = sub.getFailReason().name();
      }
      attach(handle).update(
          sub.getCustomerRef(),
          sub.getSiteRef(),
          sub.getSpireRef(),
          sub.getMode().name(),
          sub.getStatus().name(),
          sub.isRoleUpdated(),
          sub.isCalledBack(),
          sub.getFirstFail(),
          sub.getLastFailMessage(),
          failReason,
          sub.getId());
    }
  }

  private OgelSubmissionInterface attach(Handle handle) {
    return handle.attach(OgelSubmissionInterface.class);
  }
}

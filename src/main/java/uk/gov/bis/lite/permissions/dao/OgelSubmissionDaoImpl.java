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
  public List<OgelSubmission> getScheduledCallbacks() {
    try (final Handle handle = jdbi.open()) {
      return attach(handle).getScheduledCallbacks();
    }
  }

  @Override
  @Transaction
  public void create(OgelSubmission sub) {
    try (final Handle handle = jdbi.open()) {
      attach(handle).insert(
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
          sub.getId());
    }
  }

  private OgelSubmissionInterface attach(Handle handle) {
    return handle.attach(OgelSubmissionInterface.class);
  }
}

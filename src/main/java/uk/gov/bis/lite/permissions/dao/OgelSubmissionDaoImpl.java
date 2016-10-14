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
  public List<OgelSubmission> getByStatus(String status) {
    try (final Handle handle = jdbi.open()) {
      return attach(handle).getByStatus(status);
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
          sub.getJson(),
          sub.getStatus().name(),
          sub.isRoleUpdate());
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
          sub.getStatus().name(),
          sub.getId());
    }
  }

  private OgelSubmissionInterface attach(Handle handle) {
    return handle.attach(OgelSubmissionInterface.class);
  }
}

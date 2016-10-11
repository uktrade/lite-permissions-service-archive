package uk.gov.bis.lite.permissions.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.Transaction;
import uk.gov.bis.lite.permissions.dao.sqlite.OgelRegistrationInterface;
import uk.gov.bis.lite.permissions.model.OgelRegistration;

import java.util.List;

@Singleton
public class OgelRegistrationDaoImpl implements OgelRegistrationDao {

  private final DBI jdbi;

  @Inject
  public OgelRegistrationDaoImpl(@Named("jdbi") DBI jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  @Transaction
  public OgelRegistration findByLiteId(String liteId) {
    try (final Handle handle = jdbi.open()) {
      return attach(handle).findByLiteId(liteId);
    }
  }

  @Override
  @Transaction
  public List<OgelRegistration> getByStatus(String status) {
    try (final Handle handle = jdbi.open()) {
      return attach(handle).getByStatus(status);
    }
  }

  @Override
  @Transaction
  public void create(OgelRegistration ogReg) {
    try (final Handle handle = jdbi.open()) {
      attach(handle).insert(
          ogReg.getUserId(),
          ogReg.getOgelType(),
          ogReg.getLiteId(),
          ogReg.getCustomerId(),
          ogReg.getSiteId(),
          ogReg.getJson(),
          ogReg.getStatus().name());
    }
  }

  @Override
  @Transaction
  public void update(OgelRegistration ogReg) {
    try (final Handle handle = jdbi.open()) {
      attach(handle).update(
          ogReg.getCustomerId(),
          ogReg.getSiteId(),
          ogReg.getStatus().name(),
          ogReg.getId());
    }
  }


  private OgelRegistrationInterface attach(Handle handle) {
    return handle.attach(OgelRegistrationInterface.class);
  }
}

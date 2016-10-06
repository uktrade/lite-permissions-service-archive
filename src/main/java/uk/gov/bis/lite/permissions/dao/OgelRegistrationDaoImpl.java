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
  public List<OgelRegistration> getCreated() {
    try (final Handle handle = jdbi.open()) {
      OgelRegistrationInterface ori = handle.attach(OgelRegistrationInterface.class);
      return ori.getCreated();
    }
  }

  @Override
  @Transaction
  public void create(OgelRegistration ogReg) {
    try (final Handle handle = jdbi.open()) {
      OgelRegistrationInterface ori = handle.attach(OgelRegistrationInterface.class);
      ori.insert(
          ogReg.getUserId(),
          ogReg.getOgelType(),
          ogReg.getLiteId(),
          ogReg.getCustomerId(),
          ogReg.getSiteId(),
          ogReg.getJson(),
          ogReg.getStatus().name());
    }
  }
}

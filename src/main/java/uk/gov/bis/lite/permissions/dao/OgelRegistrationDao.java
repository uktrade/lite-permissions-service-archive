package uk.gov.bis.lite.permissions.dao;

import uk.gov.bis.lite.permissions.model.OgelRegistration;

import java.util.List;

public interface OgelRegistrationDao {

  void update(OgelRegistration ogReg);

  void create(OgelRegistration ogReg);

  List<OgelRegistration> getByStatus(String status);

  OgelRegistration findByLiteId(String liteId);
}

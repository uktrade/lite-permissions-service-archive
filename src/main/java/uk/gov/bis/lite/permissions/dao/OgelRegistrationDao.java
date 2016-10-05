package uk.gov.bis.lite.permissions.dao;

import uk.gov.bis.lite.permissions.model.OgelRegistration;

import java.util.List;

public interface OgelRegistrationDao {

  //OgelRegistration findById(int id);

  void create(OgelRegistration ogReg);

  List<OgelRegistration> getCreated();
}

package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.model.register.RegisterOgel;

public interface RegisterService {

  String register(RegisterOgel reg, String callbackUrl);

}

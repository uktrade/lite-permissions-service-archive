package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.api.param.RegisterParam;

public interface RegisterService {

  String register(RegisterParam reg, String callbackUrl);

  boolean isValid(RegisterParam registerParam);

  String validationInfo(RegisterParam registerParam);

  String generateSubmissionReference(RegisterParam registerParam);

}

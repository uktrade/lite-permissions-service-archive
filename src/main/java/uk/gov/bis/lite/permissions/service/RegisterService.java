package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.api.param.RegisterParam;

public interface RegisterService {

  String register(RegisterParam reg, String callbackUrl);

  boolean isRegisterParamValid(RegisterParam registerParam);

  String getRegisterParamValidationInfo(RegisterParam registerParam);

  String generateSubmissionReference(RegisterParam registerParam);

}

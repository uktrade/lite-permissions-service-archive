package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

public interface RegisterService {

  OgelSubmission getOgelSubmission(RegisterParam param);

  String register(OgelSubmission sub, String callbackUrl);

  boolean isRegisterParamValid(RegisterParam registerParam);

  String getRegisterParamValidationInfo(RegisterParam registerParam);

  String generateSubmissionReference(RegisterParam registerParam);

}

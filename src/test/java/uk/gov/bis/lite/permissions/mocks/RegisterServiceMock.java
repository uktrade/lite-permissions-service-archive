package uk.gov.bis.lite.permissions.mocks;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.RegisterService;

@Singleton
public class RegisterServiceMock implements RegisterService {

  @Override
  public OgelSubmission getOgelSubmission(RegisterParam param) {
    return null;
  }

  @Override
  public String register(OgelSubmission sub, String callbackUrl) {
    return "SUB1";
  }

  public boolean isRegisterParamValid(RegisterParam registerParam) {
    return true;
  }

  public String getRegisterParamValidationInfo(RegisterParam registerParam) {
    return "";
  }

  public String generateSubmissionReference(RegisterParam registerParam) {
    return "";
  }
}

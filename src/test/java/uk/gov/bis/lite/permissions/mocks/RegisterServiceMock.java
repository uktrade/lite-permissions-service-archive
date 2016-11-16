package uk.gov.bis.lite.permissions.mocks;

import uk.gov.bis.lite.permissions.api.param.RegisterParam;
import uk.gov.bis.lite.permissions.service.RegisterService;

public class RegisterServiceMock implements RegisterService {

  private String mockSubmissionRef;

  public RegisterServiceMock(String mockSubmissionRef) {
    this.mockSubmissionRef = mockSubmissionRef;
  }

  public String register(RegisterParam reg, String callbackUrl) {
    return mockSubmissionRef;
  }

  public boolean isValid(RegisterParam registerParam) {
    return true;
  }

  public String validationInfo(RegisterParam registerParam) {
    return "";
  }

  public String generateSubmissionReference(RegisterParam registerParam) {
    return "";
  }
}

package uk.gov.bis.lite.permissions.mocks;

import uk.gov.bis.lite.permissions.model.register.RegisterOgel;
import uk.gov.bis.lite.permissions.service.RegisterService;

public class RegisterServiceMock implements RegisterService {

  private String mockSubmissionRef;

  public RegisterServiceMock(String mockSubmissionRef) {
    this.mockSubmissionRef = mockSubmissionRef;
  }

  public String register(RegisterOgel reg, String callbackUrl) {
    return mockSubmissionRef;
  }

}

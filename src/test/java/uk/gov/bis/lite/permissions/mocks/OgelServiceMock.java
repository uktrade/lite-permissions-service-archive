package uk.gov.bis.lite.permissions.mocks;


import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.OgelService;

import java.util.Optional;

@Singleton
public class OgelServiceMock implements OgelService {

  private boolean createOgelSuccess = true;

  private int createOgelCallCount = 0;

  @Override
  public Optional<String> createOgel(OgelSubmission sub) {
    createOgelCallCount++;
    return this.createOgelSuccess ? Optional.of("SPIRE_REF_MOCK") : Optional.empty();
  }

  public void setCreateOgelSuccess(boolean createOgelSuccess) {
    this.createOgelSuccess = createOgelSuccess;
  }

  public int getCreateOgelCallCount() {
    return createOgelCallCount;
  }

  public void resetCreateOgelCallCount() {
    this.createOgelCallCount = 0;
  }
}

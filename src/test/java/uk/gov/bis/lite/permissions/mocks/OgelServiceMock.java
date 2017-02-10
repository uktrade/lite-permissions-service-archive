package uk.gov.bis.lite.permissions.mocks;


import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.Util;
import uk.gov.bis.lite.permissions.model.FailEvent;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.OgelService;

import java.util.Optional;

@Singleton
public class OgelServiceMock implements OgelService {

  private boolean createOgelSuccess = true;

  private int createOgelCallCount = 0;

  private FailEvent failEvent = null;

  @Override
  public Optional<String> createOgel(OgelSubmission sub) {
    createOgelCallCount++;
    if (!createOgelSuccess) {
      sub.setFailEvent(failEvent);
    }
    return this.createOgelSuccess ? Optional.of(Util.SPIRE_REF) : Optional.empty();
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

  public void resetFailEvent() {
    this.failEvent = null;
  }

  public void setFailEvent(FailEvent failEvent) {
    this.failEvent = failEvent;
  }
}

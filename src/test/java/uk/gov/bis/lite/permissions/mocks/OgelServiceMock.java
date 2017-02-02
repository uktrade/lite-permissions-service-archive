package uk.gov.bis.lite.permissions.mocks;


import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.OgelService;

import java.util.Optional;

public class OgelServiceMock implements OgelService {

  private boolean createOgelSuccess = true;

  @Override
  public Optional<String> createOgel(OgelSubmission sub) {
    return createOgelSuccess ? Optional.of("SPIRE_REF_MOCK") : Optional.empty();
  }

  public void setCreateOgelSuccess(boolean createOgelSuccess) {
    this.createOgelSuccess = createOgelSuccess;
  }
}

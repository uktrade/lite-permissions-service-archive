package uk.gov.bis.lite.permissions.service;

import uk.gov.bis.lite.permissions.model.OgelSubmission;

public interface CallbackService {

  void completeCallback(OgelSubmission sub);
}

package uk.gov.bis.lite.permissions.service;


import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.Optional;

public interface OgelService {

  Optional<String> createOgel(OgelSubmission sub);

}

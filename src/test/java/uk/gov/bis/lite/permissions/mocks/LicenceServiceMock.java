package uk.gov.bis.lite.permissions.mocks;

import com.google.inject.Singleton;
import uk.gov.bis.lite.permissions.api.view.LicenceView;
import uk.gov.bis.lite.permissions.api.view.LicenceView.Type;
import uk.gov.bis.lite.permissions.service.LicenceService;
import uk.gov.bis.lite.permissions.service.model.LicenceResult;
import uk.gov.bis.lite.permissions.service.model.Status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Singleton
public class LicenceServiceMock implements LicenceService {

  private boolean noResults = false;
  private boolean userNotFound = false;

  public void resetState() {
    this.noResults = false;
    this.userNotFound = false;
  }

  @Override
  public LicenceResult getLicenceByRef(String userId, String reference) {
    if (userNotFound) {
      return new LicenceResult(Status.USER_ID_NOT_FOUND, "Unable to find user with user id 1", null);
    } else if (noResults) {
      return new LicenceResult(Status.REGISTRATION_NOT_FOUND, "No licence with reference LIC/123 found for userId 1", null);
    } else {
      return new LicenceResult(Status.OK, null, buildLicences());
    }
  }

  @Override
  public LicenceResult getAllLicences(String userId) {
    if (userNotFound) {
      return new LicenceResult(Status.USER_ID_NOT_FOUND, "Unable to find user with user id 1", null);
    } else if (noResults) {
      return new LicenceResult(Status.OK, null, new ArrayList<>());
    } else {
      return new LicenceResult(Status.OK, null, buildLicences());
    }
  }

  @Override
  public LicenceResult getLicencesByType(String userId, String type) {
    if (userNotFound) {
      return new LicenceResult(Status.USER_ID_NOT_FOUND, "Unable to find user with user id 1", null);
    } else if (noResults) {
      return new LicenceResult(Status.OK, null, new ArrayList<>());
    } else {
      return new LicenceResult(Status.OK, null, buildLicences());
    }
  }

  private List<LicenceView> buildLicences() {
    LicenceView licenceView = new LicenceView();
    licenceView.setLicenceRef("LIC/123");
    licenceView.setOriginalAppId("originalAppId");
    licenceView.setOriginalExporterRef("originalExporterRef");
    licenceView.setCustomerId("customerId");
    licenceView.setSiteId("siteId");
    licenceView.setType(Type.SIEL);
    licenceView.setSubType(null);
    licenceView.setIssueDate(LocalDate.of(2010, 4, 21));
    licenceView.setExpiryDate(LocalDate.of(2010, 4, 21));
    licenceView.setStatus(LicenceView.Status.ACTIVE);
    licenceView.setCountryList(Arrays.asList("Germany", "France"));
    licenceView.setExternalDocumentUrl("externalDocumentUrl");
    return Collections.singletonList(licenceView);
  }

  public void setNoResults(boolean noResults) {
    this.noResults = noResults;
  }

  public void setUserNotFound(boolean userNotFound) {
    this.userNotFound = userNotFound;
  }

}

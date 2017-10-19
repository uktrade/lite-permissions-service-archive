package uk.gov.bis.lite.permissions.spire.model;

public class SpireLicence {
  private String licenceReference;
  private String originalApplicationReference;
  private String exporterApplicationReference;
  private String sarId;
  private String siteId;
  private String licenceType;
  private String licenceIssueDate;
  private String licenceExpiryDate;
  private String licenceStatus; //enum
  private String licenceCountryList; // List type
  private String externalDocumentUrl;

  public String getLicenceReference() {
    return licenceReference;
  }

  public SpireLicence setLicenceReference(String licenceReference) {
    this.licenceReference = licenceReference;
    return this;
  }

  public String getOriginalApplicationReference() {
    return originalApplicationReference;
  }

  public SpireLicence setOriginalApplicationReference(String originalApplicationReference) {
    this.originalApplicationReference = originalApplicationReference;
    return this;
  }

  public String getExporterApplicationReference() {
    return exporterApplicationReference;
  }

  public SpireLicence setExporterApplicationReference(String exporterApplicationReference) {
    this.exporterApplicationReference = exporterApplicationReference;
    return this;
  }

  public String getSarId() {
    return sarId;
  }

  public SpireLicence setSarId(String sarId) {
    this.sarId = sarId;
    return this;
  }

  public String getSiteId() {
    return siteId;
  }

  public SpireLicence setSiteId(String siteId) {
    this.siteId = siteId;
    return this;
  }

  public String getLicenceType() {
    return licenceType;
  }

  public SpireLicence setLicenceType(String licenceType) {
    this.licenceType = licenceType;
    return this;
  }

  public String getLicenceIssueDate() {
    return licenceIssueDate;
  }

  public SpireLicence setLicenceIssueDate(String licenceIssueDate) {
    this.licenceIssueDate = licenceIssueDate;
    return this;
  }

  public String getLicenceExpiryDate() {
    return licenceExpiryDate;
  }

  public SpireLicence setLicenceExpiryDate(String licenceExpiryDate) {
    this.licenceExpiryDate = licenceExpiryDate;
    return this;
  }

  public String getLicenceStatus() {
    return licenceStatus;
  }

  public SpireLicence setLicenceStatus(String licenceStatus) {
    this.licenceStatus = licenceStatus;
    return this;
  }

  public String getLicenceCountryList() {
    return licenceCountryList;
  }

  public SpireLicence setLicenceCountryList(String licenceCountryList) {
    this.licenceCountryList = licenceCountryList;
    return this;
  }

  public String getExternalDocumentUrl() {
    return externalDocumentUrl;
  }

  public SpireLicence setExternalDocumentUrl(String externalDocumentUrl) {
    this.externalDocumentUrl = externalDocumentUrl;
    return this;
  }
}

package uk.gov.bis.lite.permissions.api.view;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class LicenceView {
  private String licenceRef;
  private String originalAppId;
  private String originalExporterRef;
  private String customerId;
  private String siteId;
  private String type;
  private String subType;

  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
  private LocalDate issueDate;

  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
  private LocalDate expiryDate;

  private Status status;
  private List<String> countryList;
  private String externalDocumentUrl;

  public LicenceView() {
  }

  public String getLicenceRef() {
    return licenceRef;
  }

  public LicenceView setLicenceRef(String licenceRef) {
    this.licenceRef = licenceRef;
    return this;
  }

  public String getOriginalAppId() {
    return originalAppId;
  }

  public LicenceView setOriginalAppId(String originalAppId) {
    this.originalAppId = originalAppId;
    return this;
  }

  public String getOriginalExporterRef() {
    return originalExporterRef;
  }

  public LicenceView setOriginalExporterRef(String originalExporterRef) {
    this.originalExporterRef = originalExporterRef;
    return this;
  }

  public String getCustomerId() {
    return customerId;
  }

  public LicenceView setCustomerId(String customerId) {
    this.customerId = customerId;
    return this;
  }

  public String getSiteId() {
    return siteId;
  }

  public LicenceView setSiteId(String siteId) {
    this.siteId = siteId;
    return this;
  }

  public String getType() {
    return type;
  }

  public LicenceView setType(String type) {
    this.type = type;
    return this;
  }

  public String getSubType() {
    return subType;
  }

  public LicenceView setSubType(String subType) {
    this.subType = subType;
    return this;
  }

  public LocalDate getIssueDate() {
    return issueDate;
  }

  public LicenceView setIssueDate(LocalDate issueDate) {
    this.issueDate = issueDate;
    return this;
  }

  public LocalDate getExpiryDate() {
    return expiryDate;
  }

  public LicenceView setExpiryDate(LocalDate expiryDate) {
    this.expiryDate = expiryDate;
    return this;
  }

  public Status getStatus() {
    return status;
  }

  public LicenceView setStatus(Status status) {
    this.status = status;
    return this;
  }

  public List<String> getCountryList() {
    return countryList;
  }

  public LicenceView setCountryList(List<String> countryList) {
    this.countryList = countryList;
    return this;
  }

  public String getExternalDocumentUrl() {
    return externalDocumentUrl;
  }

  public LicenceView setExternalDocumentUrl(String externalDocumentUrl) {
    this.externalDocumentUrl = externalDocumentUrl;
    return this;
  }
}

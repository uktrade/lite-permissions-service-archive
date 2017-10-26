package uk.gov.bis.lite.permissions.api.view;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class LicenceView {
  private String reference;
  private String originalApplicationReference;
  private String exporterApplicationReference;
  private String sarId;
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

  public String getReference() {
    return reference;
  }

  public LicenceView setReference(String reference) {
    this.reference = reference;
    return this;
  }

  public String getOriginalApplicationReference() {
    return originalApplicationReference;
  }

  public LicenceView setOriginalApplicationReference(String originalApplicationReference) {
    this.originalApplicationReference = originalApplicationReference;
    return this;
  }

  public String getExporterApplicationReference() {
    return exporterApplicationReference;
  }

  public LicenceView setExporterApplicationReference(String exporterApplicationReference) {
    this.exporterApplicationReference = exporterApplicationReference;
    return this;
  }

  public String getSarId() {
    return sarId;
  }

  public LicenceView setSarId(String sarId) {
    this.sarId = sarId;
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

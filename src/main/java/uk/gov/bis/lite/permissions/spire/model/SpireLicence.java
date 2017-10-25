package uk.gov.bis.lite.permissions.spire.model;

import java.util.List;

public class SpireLicence {
  private String reference;
  private String originalApplicationReference;
  private String exporterApplicationReference;
  private String sarId;
  private String siteId;
  private String type;
  private String subType;
  private String issueDate;
  private String expiryDate;
  private String status;
  private List<String> countryList;
  private String externalDocumentUrl;

  public String getReference() {
    return reference;
  }

  public SpireLicence setReference(String reference) {
    this.reference = reference;
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

  public String getType() {
    return type;
  }

  public SpireLicence setType(String type) {
    this.type = type;
    return this;
  }

  public String getSubType() {
    return subType;
  }

  public SpireLicence setSubType(String subType) {
    this.subType = subType;
    return this;
  }

  public String getIssueDate() {
    return issueDate;
  }

  public SpireLicence setIssueDate(String issueDate) {
    this.issueDate = issueDate;
    return this;
  }

  public String getExpiryDate() {
    return expiryDate;
  }

  public SpireLicence setExpiryDate(String expiryDate) {
    this.expiryDate = expiryDate;
    return this;
  }

  public String getStatus() {
    return status;
  }

  public SpireLicence setStatus(String status) {
    this.status = status;
    return this;
  }

  public List<String> getCountryList() {
    return countryList;
  }

  public SpireLicence setCountryList(List<String> countryList) {
    this.countryList = countryList;
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

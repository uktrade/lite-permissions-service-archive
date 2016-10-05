package uk.gov.bis.lite.permissions.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OgelRegistration {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelRegistration.class);

  private int id;
  private String liteId;
  private String ogelType;
  private String userId;
  private String customerId;
  private String siteId;
  private Status status;
  private String created;

  public enum Status {
    CREATED, PENDING;
  }

  public OgelRegistration(int id) {
    this.id = id;
  }

  public OgelRegistration(String userId, String ogelType) {
    this.userId = userId;
    this.ogelType = ogelType;
    this.status = Status.CREATED;
  }

  public String getLiteId() {
    return liteId;
  }

  public void setLiteId(String liteId) {
    this.liteId = liteId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getSiteId() {
    return siteId;
  }

  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getOgelType() {
    return ogelType;
  }

  public void setOgelType(String type) {
    this.ogelType = ogelType;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getCreated() {
    return created;
  }

  public void setCreated(String created) {
    this.created = created;
  }
}

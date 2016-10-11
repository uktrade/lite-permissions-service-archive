package uk.gov.bis.lite.permissions.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.model.request.RegisterOgel;
import uk.gov.bis.lite.permissions.util.Util;

import java.io.IOException;

public class OgelRegistration {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelRegistration.class);

  private int id;
  private String userId;
  private String ogelType;
  private Status status;
  private String liteId;
  private String customerId;
  private String siteId;
  private String json;
  private String created;
  private String approvalRequired;
  private String approvalGranted;

  public enum Status {
    PENDING, CUSTOMER, SITE, SITE_PERMISSION, COMPLETED;
  }

  public OgelRegistration(int id) {
    this.id = id;
  }

  public OgelRegistration(String userId, String ogelType) {
    this.userId = userId;
    this.ogelType = ogelType;
    this.status = Status.PENDING;
  }

  public void updateStatus() {
    if(needsCustomer()) {
      this.status = Status.CUSTOMER;
    } else if(needsSite()) {
      this.status = Status.SITE;
    } else {
      this.status = Status.SITE_PERMISSION;
    }
  }

  public void setInitialStatus() {
    if(status.equals(Status.PENDING)) {
      if(StringUtils.isBlank(customerId)) {
        this.status = Status.CUSTOMER;
      } else if (StringUtils.isBlank(siteId)) {
        this.status = Status.SITE;
      } else {
        this.status = Status.SITE_PERMISSION;
      }
    }
  }

  private boolean needsCustomer() {
    return Util.isBlank(customerId);
  }

  private boolean needsSite() {
    return Util.isBlank(siteId);
  }

  public RegisterOgel getRegisterOgelFromJson() {
    RegisterOgel regOgel = null;
    ObjectMapper mapper = new ObjectMapper();
    try {
      regOgel = mapper.readValue(this.getJson(), RegisterOgel.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return regOgel;
  }

  public String getJson() {
    return json;
  }

  public void setJson(String json) {
    this.json = json;
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

  public void setOgelType(String ogelType) {
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

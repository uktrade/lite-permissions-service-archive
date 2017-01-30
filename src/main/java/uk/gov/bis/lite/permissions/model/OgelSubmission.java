package uk.gov.bis.lite.permissions.model;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.util.Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OgelSubmission {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelSubmission.class);

  private int id;
  private String userId;
  private String adminUserId;
  private String ogelType;
  private Mode mode;
  private Stage stage;
  private Status status;
  private String submissionRef;
  private String customerRef;
  private String siteRef;
  private String spireRef;
  private String firstFail;
  private String lastFailMessage;
  private CallbackView.FailReason failReason;
  private String callbackUrl;
  private boolean calledBack;
  private String json;
  private String created;
  private boolean roleUpdate;
  private boolean roleUpdated;

  private static DateTimeFormatter ogelSubmissionDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /**
   * IMMEDIATE      - submission is being processed immediately, through all stages
   * SCHEDULED      - submission processed by scheduled jobs
   */
  public enum Mode {
    IMMEDIATE, SCHEDULED;
  }

  /**
   * CREATED    - initial status on creation
   * CUSTOMER   - we need to create a Customer and populate customerId with resulting sarRef
   * SITE       - we need to create a Site and populate siteId with resulting siteRef
   * USER_ROLE  - we need to update user role permissions
   * OGEL       - we need to create Ogel via Spire
   */
  public enum Stage {
    CREATED, CUSTOMER, SITE, USER_ROLE, OGEL;
  }

  /**
   * ACTIVE      - submission is being processed through stages
   * COMPLETE    - stage processing has been completed
   * CANCELLED   - stage processing has been cancelled
   * TERMINATED  - stage processing has been terminated
   */
  public enum Status {
    ACTIVE, COMPLETE, CANCELLED, TERMINATED;
  }

  public OgelSubmission(int id) {
    this.id = id;
  }

  public OgelSubmission(String userId, String ogelType) {
    this.userId = userId;
    this.ogelType = ogelType;
    this.mode = Mode.IMMEDIATE;
    this.stage = Stage.CREATED;
    this.status = Status.ACTIVE;
  }

  /**
   * RequestId is used to identify a submission to external services. It is made up
   * from the submissionRef plus the id
   */
  public String getRequestId() {
    return submissionRef + "_" + id;
  }

  public boolean isProcessingCompleted() {
    return !status.equals(Status.ACTIVE);
  }

  public boolean isStatusComplete() {
    return status.equals(Status.COMPLETE);
  }

  /**
   * OgelSubmission is COMPLETE and we have a SpireRef
   */
  public boolean isCompleteSuccess() {
    return status.equals(Status.COMPLETE) && !StringUtils.isBlank(spireRef);
  }

  public boolean isStatusCancelled() {
    return status.equals(Status.CANCELLED);
  }

  public boolean isModeScheduled() {
    return mode.equals(OgelSubmission.Mode.SCHEDULED);
  }

  public void cancelProcessing() {
    this.status = Status.CANCELLED;
  }

  public boolean hasFail() {
    return !StringUtils.isBlank(firstFail);
  }

  public boolean hasAdminUserId() {
    return !StringUtils.isBlank(adminUserId);
  }

  public LocalDateTime getFirstFailDateTime() {
    LocalDateTime date = null;
    if (!StringUtils.isBlank(firstFail)) {
      date = LocalDateTime.parse(firstFail, ogelSubmissionDateFormatter);
    }
    return date;
  }

  public void setFirstFailDateTime() {
    LocalDateTime now = LocalDateTime.now();
    firstFail = now.format(ogelSubmissionDateFormatter);
  }

  public void setScheduledMode() {
    mode = Mode.SCHEDULED;
  }

  public void updateStatusToComplete() {
    status = Status.COMPLETE;
  }

  public void updateStatusToTerminated() {
    status = Status.TERMINATED;
  }

  public void updateStatusToCancelled() {
    status = Status.CANCELLED;
  }

  public void updateToNextStage() {
    this.stage = getNextStage();
    if(hasCompletedStage(this.stage)) {
      updateToNextStage();
    }
  }

  private Stage getNextStage() {
    Stage stage = null;
    if (this.stage.equals(Stage.CREATED)) {
      stage = Stage.CUSTOMER;
    } else if (this.stage.equals(Stage.CUSTOMER)) {
      stage = Stage.SITE;
    } else if (this.stage.equals(Stage.SITE)) {
      stage = Stage.USER_ROLE;
    }  else if (this.stage.equals(Stage.USER_ROLE)) {
      stage = Stage.OGEL;
    }
    return stage;
  }

  public boolean hasCompletedStage(Stage stage) {
    boolean completed = false;
    if(stage.equals(Stage.CREATED)) {
      completed = true;
    } else if(stage.equals(Stage.CUSTOMER)) {
      completed = !Util.isBlank(customerRef);
    } else if(stage.equals(Stage.SITE)) {
      completed = !Util.isBlank(siteRef);
    } else if(stage.equals(Stage.USER_ROLE)) {
      completed = !roleUpdate || roleUpdated;
    } else if(stage.equals(Stage.OGEL)) {
      completed = !Util.isBlank(spireRef);
    }
    return completed;
  }

  public boolean hasCompletedAllStages(Stage... stages) {
    boolean allCompleted = true;
    for(Stage stage : stages){
      if(!hasCompletedStage(stage)) {
        allCompleted = false;
        break;
      }
    }
    return allCompleted;
  }


  public String getJson() {
    return json;
  }

  public void setJson(String json) {
    this.json = json;
  }

  public String getSubmissionRef() {
    return submissionRef;
  }

  public void setSubmissionRef(String submissionRef) {
    this.submissionRef = submissionRef;
  }

  public String getCustomerRef() {
    return customerRef;
  }

  public void setCustomerRef(String customerRef) {
    this.customerRef = customerRef;
  }

  public String getSiteRef() {
    return siteRef;
  }

  public void setSiteRef(String siteRef) {
    this.siteRef = siteRef;
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

  public boolean isRoleUpdate() {
    return roleUpdate;
  }

  public void setRoleUpdate(boolean roleUpdate) {
    this.roleUpdate = roleUpdate;
  }

  public String getSpireRef() {
    return spireRef;
  }

  public void setSpireRef(String spireRef) {
    this.spireRef = spireRef;
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  public boolean isRoleUpdated() {
    return roleUpdated;
  }

  public void setRoleUpdated(boolean roleUpdated) {
    this.roleUpdated = roleUpdated;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

  public boolean isCalledBack() {
    return calledBack;
  }

  public void setCalledBack(boolean calledBack) {
    this.calledBack = calledBack;
  }

  public String getFirstFail() {
    return firstFail;
  }

  public void setFirstFail(String firstFail) {
    this.firstFail = firstFail;
  }

  public String getLastFailMessage() {
    return lastFailMessage;
  }

  public void setLastFailMessage(String lastFailMessage) {
    this.lastFailMessage = lastFailMessage;
  }

  public CallbackView.FailReason getFailReason() {
    return failReason;
  }

  public void setFailReason(CallbackView.FailReason failReason) {
    this.failReason = failReason;
  }

  public String getAdminUserId() {
    return adminUserId;
  }

  public void setAdminUserId(String adminUserId) {
    this.adminUserId = adminUserId;
  }

  public Stage getStage() {
    return stage;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }
}

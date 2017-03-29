package uk.gov.bis.lite.permissions.model;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.api.view.CallbackView;

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
  private String lastFail;
  private String lastFailMessage;
  private FailReason failReason;
  private String callbackUrl;
  private boolean calledBack;
  private int callBackFailCount = 0;
  private String json;
  private String created;
  private boolean roleUpdate;
  private boolean roleUpdated;

  private transient FailEvent failEvent = null;

  private static DateTimeFormatter ogelSubmissionDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
   * ACTIVE      - submission is being processed
   * COMPLETE    - processing has been completed
   * TERMINATED  - processing has been terminated
   */
  public enum Status {
    ACTIVE, COMPLETE, TERMINATED;
  }

  /**
   * FailReasons have mapping to CallbackView.Result
   */
  public enum FailReason {

    PERMISSION_DENIED(CallbackView.Result.PERMISSION_DENIED),
    SITE_ALREADY_REGISTERED(CallbackView.Result.SITE_ALREADY_REGISTERED),
    BLACKLISTED(CallbackView.Result.BLACKLISTED),
    ENDPOINT_ERROR(CallbackView.Result.FAILED),
    UNCLASSIFIED(CallbackView.Result.FAILED);

    private final CallbackView.Result result;

    FailReason(CallbackView.Result result) {
      this.result = result;
    }

    public CallbackView.Result toResult() {
      return result;
    }
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

  public String getProcessState() {
    return mode.name() + "/" + stage.name() + "/" + status.name();
  }

  /**
   * RequestId is used to identify a submission to external services. It is made up
   * from the submissionRef plus the id
   */
  public String getRequestId() {
    return submissionRef + id;
  }

  public boolean isProcessingCompleted() {
    return !status.equals(Status.ACTIVE);
  }

  public boolean isStatusComplete() {
    return status.equals(Status.COMPLETE);
  }

  /**
   * OgelSubmission Status is COMPLETE and we have a SpireRef
   */
  public boolean isCompletedWithSpireRef() {
    return status.equals(Status.COMPLETE) && !StringUtils.isBlank(spireRef);
  }

  public boolean isStatusTerminated() {
    return status.equals(Status.TERMINATED);
  }

  public boolean isModeScheduled() {
    return mode.equals(OgelSubmission.Mode.SCHEDULED);
  }

  public void terminateProcessing() {
    this.status = Status.TERMINATED;
  }

  public boolean hasFailReason() {
    return failReason != null;
  }

  public boolean hasFail() {
    return !StringUtils.isBlank(firstFail);
  }

  public boolean hasFailEvent() {
    return failEvent != null;
  }

  public void clearFailEvent() {
    this.failEvent = null;
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

  public void setLastFailDateTime() {
    LocalDateTime now = LocalDateTime.now();
    lastFail = now.format(ogelSubmissionDateFormatter);
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

  public FailReason getFailReason() {
    return failReason;
  }

  public void setFailReason(FailReason failReason) {
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

  public FailEvent getFailEvent() {
    return failEvent;
  }

  public void setFailEvent(FailEvent failEvent) {
    this.failEvent = failEvent;
  }

  public String getLastFail() {
    return lastFail;
  }

  public void setLastFail(String lastFail) {
    this.lastFail = lastFail;
  }

  public int getCallBackFailCount() {
    return callBackFailCount;
  }

  public void setCallBackFailCount(int callBackFailCount) {
    this.callBackFailCount = callBackFailCount;
  }
}

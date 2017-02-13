package uk.gov.bis.lite.permissions;


import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.ProcessSubmissionServiceImpl;

public class Util {

  public final static String MOCK_CALLBACK_URL = "/callback";
  public static int MOCK_ID = 1;
  public static String SUBMISSION_REF = "SUBMISSION_REF";
  public static String SPIRE_REF = "SPIRE_REF";
  public static String CUSTOMER_REF = "CUSTOMER_REF";
  public static String SITE_REF = "SITE_REF";
  public static String USER_ID = "USER_ID";
  public static String OGEL_TYPE = "OGEL_TYPE";

  public static String ERROR_MESSAGE = "ERROR_MESSAGE";

  // Stage
  public static OgelSubmission.Stage STAGE_CREATED = OgelSubmission.Stage.CREATED;
  public static OgelSubmission.Stage STAGE_CUSTOMER = OgelSubmission.Stage.CUSTOMER;
  public static OgelSubmission.Stage STAGE_SITE = OgelSubmission.Stage.SITE;
  public static OgelSubmission.Stage STAGE_USER_ROLE = OgelSubmission.Stage.USER_ROLE;
  public static OgelSubmission.Stage STAGE_OGEL = OgelSubmission.Stage.OGEL;

  // Origins
  public static ProcessSubmissionServiceImpl.Origin ORIGIN_SITE = ProcessSubmissionServiceImpl.Origin.SITE;
  public static ProcessSubmissionServiceImpl.Origin ORIGIN_CUSTOMER = ProcessSubmissionServiceImpl.Origin.CUSTOMER;
  public static ProcessSubmissionServiceImpl.Origin ORIGIN_OGEL_CREATE = ProcessSubmissionServiceImpl.Origin.OGEL_CREATE;
  public static ProcessSubmissionServiceImpl.Origin ORIGIN_USER_ROLE = ProcessSubmissionServiceImpl.Origin.USER_ROLE;
  public static ProcessSubmissionServiceImpl.Origin ORIGIN_CALLBACK = ProcessSubmissionServiceImpl.Origin.CALLBACK;

  // Status
  public static OgelSubmission.Status STATUS_ACTIVE = OgelSubmission.Status.ACTIVE;
  public static OgelSubmission.Status STATUS_COMPLETE = OgelSubmission.Status.COMPLETE;
  public static OgelSubmission.Status STATUS_TERMINATED = OgelSubmission.Status.TERMINATED;

  // FailReasons
  public static OgelSubmission.FailReason PERMISSION_DENIED = OgelSubmission.FailReason.PERMISSION_DENIED;
  public static OgelSubmission.FailReason BLACKLISTED = OgelSubmission.FailReason.BLACKLISTED;
  public static OgelSubmission.FailReason SITE_ALREADY_REGISTERED = OgelSubmission.FailReason.SITE_ALREADY_REGISTERED;
  public static OgelSubmission.FailReason ENDPOINT_ERROR = OgelSubmission.FailReason.ENDPOINT_ERROR;
  public static OgelSubmission.FailReason UNCLASSIFIED = OgelSubmission.FailReason.UNCLASSIFIED;

  public static OgelSubmission getMockOgelSubmission(String userId) {
    OgelSubmission sub = getMockOgelSubmission();
    sub.setUserId(userId);
    return sub;
  }

  public static OgelSubmission getMockActiveOgelSubmission() {
    OgelSubmission sub = getMockOgelSubmission();
    sub.setStatus(OgelSubmission.Status.ACTIVE);
    return sub;
  }

  public static OgelSubmission getMockSubmission(String submissionRef) {
    OgelSubmission sub = new OgelSubmission(USER_ID, OGEL_TYPE);
    sub.setScheduledMode();
    sub.setSubmissionRef(submissionRef);
    sub.setRoleUpdate(true);
    return sub;
  }

  public static OgelSubmission getMockWithFailReason(OgelSubmission.FailReason failReason) {
    OgelSubmission sub = getMockOgelSubmission();
    sub.setFailReason(failReason);
    sub.setSpireRef(null);
    return sub;
  }

  public static OgelSubmission getMockOgelSubmission() {
    OgelSubmission sub = new OgelSubmission(USER_ID, OGEL_TYPE);
    sub.setScheduledMode();
    sub.setId(MOCK_ID);
    sub.setSubmissionRef(SUBMISSION_REF);
    sub.setStage(OgelSubmission.Stage.OGEL);
    sub.setStatus(OgelSubmission.Status.COMPLETE);
    sub.setCustomerRef(CUSTOMER_REF);
    sub.setSiteRef(SITE_REF);
    sub.setSpireRef(SPIRE_REF);
    sub.setRoleUpdate(true);
    sub.setRoleUpdated(true);
    sub.setCalledBack(false);
    sub.setCallbackUrl(MOCK_CALLBACK_URL);
    return sub;
  }
}

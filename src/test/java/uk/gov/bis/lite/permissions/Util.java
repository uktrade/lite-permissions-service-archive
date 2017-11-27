package uk.gov.bis.lite.permissions;

import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.ProcessSubmissionServiceImpl;

import java.util.Map;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

public class Util {

  public static final String MOCK_CALLBACK_URL = "/callback";
  public static final int MOCK_ID = 1;
  public static final String SUBMISSION_REF = "SUBMISSION_REF";
  public static final String SPIRE_REF = "SPIRE_REF";
  public static final String CUSTOMER_REF = "CUSTOMER_REF";
  public static final String SITE_REF = "SITE_REF";
  public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
  private static final String USER_ID = "USER_ID";
  private static final String OGEL_TYPE = "OGEL_TYPE";

  // Stage
  public static final OgelSubmission.Stage STAGE_CREATED = OgelSubmission.Stage.CREATED;
  public static final OgelSubmission.Stage STAGE_CUSTOMER = OgelSubmission.Stage.CUSTOMER;
  public static final OgelSubmission.Stage STAGE_SITE = OgelSubmission.Stage.SITE;
  public static final OgelSubmission.Stage STAGE_USER_ROLE = OgelSubmission.Stage.USER_ROLE;
  public static final OgelSubmission.Stage STAGE_OGEL = OgelSubmission.Stage.OGEL;

  // Origins
  public static final ProcessSubmissionServiceImpl.Origin ORIGIN_SITE = ProcessSubmissionServiceImpl.Origin.SITE;
  public static final ProcessSubmissionServiceImpl.Origin ORIGIN_CUSTOMER = ProcessSubmissionServiceImpl.Origin.CUSTOMER;
  public static final ProcessSubmissionServiceImpl.Origin ORIGIN_OGEL_CREATE = ProcessSubmissionServiceImpl.Origin.OGEL_CREATE;
  public static final ProcessSubmissionServiceImpl.Origin ORIGIN_USER_ROLE = ProcessSubmissionServiceImpl.Origin.USER_ROLE;
  public static final ProcessSubmissionServiceImpl.Origin ORIGIN_CALLBACK = ProcessSubmissionServiceImpl.Origin.CALLBACK;

  // Status
  public static final OgelSubmission.Status STATUS_ACTIVE = OgelSubmission.Status.ACTIVE;
  public static final OgelSubmission.Status STATUS_COMPLETE = OgelSubmission.Status.COMPLETE;
  public static final OgelSubmission.Status STATUS_TERMINATED = OgelSubmission.Status.TERMINATED;

  // FailReasons
  public static final OgelSubmission.FailReason PERMISSION_DENIED = OgelSubmission.FailReason.PERMISSION_DENIED;
  public static final OgelSubmission.FailReason BLACKLISTED = OgelSubmission.FailReason.BLACKLISTED;
  public static final OgelSubmission.FailReason SITE_ALREADY_REGISTERED = OgelSubmission.FailReason.SITE_ALREADY_REGISTERED;
  public static final OgelSubmission.FailReason ENDPOINT_ERROR = OgelSubmission.FailReason.ENDPOINT_ERROR;
  public static final OgelSubmission.FailReason UNCLASSIFIED = OgelSubmission.FailReason.UNCLASSIFIED;

  public static OgelSubmission getMockOgelSubmission(String userId) {
    OgelSubmission sub = getMockOgelSubmission();
    sub.setUserId(userId);
    return sub;
  }

  public static OgelSubmission getMockCallbackOgelSubmission() {
    OgelSubmission sub = getMockOgelSubmission();
    sub.setStatus(OgelSubmission.Status.COMPLETE);
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

  public static Map<String, String> getResponseMap(Response response) {
    return response.readEntity(new GenericType<Map<String, String>>() {
    });
  }

}

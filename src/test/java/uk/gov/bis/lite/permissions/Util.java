package uk.gov.bis.lite.permissions;


import uk.gov.bis.lite.permissions.model.OgelSubmission;

public class Util {

  public final static String MOCK_CALLBACK_URL = "/callback";
  public final static int MOCK_ID = 1;
  public final static String SUBMISSION_REF = "SUBMISSION_REF";
  public final static String SPIRE_REF = "SPIRE_REF";
  public final static String CUSTOMER_REF = "CUSTOMER_REF";
  public final static String SITE_REF = "SITE_REF";
  public final static String USER_ID = "USER_ID";
  public final static String OGEL_TYPE = "OGEL_TYPE";

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

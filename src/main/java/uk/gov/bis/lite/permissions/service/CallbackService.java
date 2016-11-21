package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.util.Util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

@Singleton
public class CallbackService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CallbackService.class);

  private static String CALLBACK_STATUS_SUCCESS = "SUCCESS";
  private static String CALLBACK_STATUS_FAILED = "FAILED";

  private Client httpClient;
  private OgelSubmissionDao submissionDao;
  private FailService failService;

  // Search terms used to determine FailReason from error message
  private static String TERM_USER_LACKS_SITE_PRIVILEGES = "USER_LACKS_SITE_PRIVILEGES";
  private static String TERM_USER_LACKS_PRIVILEGES = "USER_LACKS_PRIVILEGES";
  private static String TERM_LICENSE_ALREADY_EXISTS = "There is already a licence for OGEL ref";
  private static String TERM_SITE_ALREADY_REGISTERED = "SITE_ALREADY_REGISTERED";
  public static String TERM_BLACKLISTED = "BLACKLISTED";
  private static String TERM_SOAP_FAULT = "soap:Fault";
  private static String TERM_CUSTOMER_NAME_ALREADY_EXISTS = "Customer name already exists";
  private static String TERM_UNCLASSIFIED_ERROR = "UNCLASSIFIED_ERROR";

  private static final Set<String> errorTerms = new HashSet<>(Arrays.asList(
      new String[]{TERM_USER_LACKS_SITE_PRIVILEGES, TERM_USER_LACKS_PRIVILEGES, TERM_LICENSE_ALREADY_EXISTS,
          TERM_SITE_ALREADY_REGISTERED, TERM_BLACKLISTED, TERM_SOAP_FAULT, TERM_CUSTOMER_NAME_ALREADY_EXISTS, TERM_UNCLASSIFIED_ERROR}
  ));

  @Inject
  public CallbackService(Client httpClient, OgelSubmissionDao submissionDao, FailService failService) {
    this.httpClient = httpClient;
    this.submissionDao = submissionDao;
    this.failService = failService;
  }

  void completeCallback(OgelSubmission sub) {
    if (sub != null && sub.hasCompleted() && !sub.isCalledBack()) {
      try {
        Response response = doCallback(sub.getCallbackUrl(), getCallbackView(sub));
        if (isOk(response)) {
          sub.setCalledBack(true);
          submissionDao.update(sub);
          LOGGER.info("CALLBACK completed [" + sub.getRequestId() + "]");
        } else {
          failService.fail(sub, CallbackView.FailReason.UNCLASSIFIED, FailService.Origin.CALLBACK, Util.info(response));
        }
      } catch (ProcessingException e) {
        failService.fail(sub, CallbackView.FailReason.UNCLASSIFIED, FailService.Origin.CALLBACK, Util.info(e));
      }
    } else {
      LOGGER.warn("OgelSubmission has not completed its processing. Postponing callback");
    }
  }

  private CallbackView getCallbackView(OgelSubmission sub) {
    CallbackView view = new CallbackView();
    if (sub.isStatusSuccess()) {
      view.setRequestId(sub.getRequestId());
      view.setStatus(CALLBACK_STATUS_SUCCESS);
      view.setRegistrationReference(sub.getSpireRef());
    }
    if (sub.isStatusError()) {
      view.setRequestId(sub.getRequestId());
      view.setStatus(CALLBACK_STATUS_FAILED);
      CallbackView.FailReason failReason = sub.getFailReason();
      if (failReason != null) {
        view.setFailReason(failReason);
      } else {
        view.setFailReason(getFailReasonFromMessage(sub.getLastFailMessage()));
      }
    }
    return view;
  }

  private CallbackView.FailReason getFailReasonFromMessage(String message) {
    CallbackView.FailReason reason = CallbackView.FailReason.UNCLASSIFIED;
    String term = errorTerms.stream().filter(message::contains).findFirst().orElse(CallbackView.FailReason.UNCLASSIFIED.name());
    if (term.equals(TERM_USER_LACKS_SITE_PRIVILEGES) || term.equals(TERM_USER_LACKS_PRIVILEGES)) {
      reason = CallbackView.FailReason.PERMISSION_DENIED;
    } else if (term.equals(TERM_SITE_ALREADY_REGISTERED)) {
      reason = CallbackView.FailReason.SITE_ALREADY_REGISTERED;
    } else if (term.equals(TERM_BLACKLISTED)) {
      reason = CallbackView.FailReason.BLACKLISTED;
    } else if (term.equals(TERM_SOAP_FAULT)) {
      reason = CallbackView.FailReason.ENDPOINT_ERROR;
    }
    return reason;
  }


  private Response doCallback(String url, CallbackView param) {
    // TODO remove once development is finished
    //url = "http://localhost:8123/callback"; // temp for development
    LOGGER.info("Attempting callback [" + url + "] ...");
    return httpClient.target(url).request().post(Entity.json(param));
  }

  private boolean isOk(Response response) {
    return response != null && (response.getStatus() == Response.Status.OK.getStatusCode());
  }
}

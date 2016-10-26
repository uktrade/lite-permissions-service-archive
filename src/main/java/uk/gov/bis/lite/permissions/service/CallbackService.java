package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.model.callback.CallbackItem;

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

  @Inject
  public CallbackService(Client httpClient, OgelSubmissionDao submissionDao, FailService failService) {
    this.httpClient = httpClient;
    this.submissionDao = submissionDao;
    this.failService = failService;
  }

  void completeCallback(OgelSubmission sub) {
    if (sub != null && sub.hasCompleted()) {
      try {
        Response response = doCallback(sub.getCallbackUrl(), getCallbackItem(sub));
        if (isOk(response)) {
          sub.setCalledBack(true);
          submissionDao.update(sub);
          LOGGER.info("CALLBACK completed [" + sub.getSubmissionRef() + "]");
        } else {
          failService.fail(sub, response, FailService.Origin.CALLBACK);
        }
      } catch (ProcessingException e) {
        failService.fail(sub, e, FailService.Origin.CALLBACK);
      }
    } else {
      LOGGER.warn("OgelSubmission has not completed its processing. Postponing callback");
    }
  }

  private CallbackItem getCallbackItem(OgelSubmission sub) {
    CallbackItem item = new CallbackItem();
    if (sub.isStatusSuccess()) {
      item.setRequestId(sub.getSubmissionRef());
      item.setStatus(CALLBACK_STATUS_SUCCESS);
      item.setRegistrationReference(sub.getSpireRef());
    }
    if (sub.isStatusError()) {
      item.setRequestId(sub.getSubmissionRef());
      item.setStatus(CALLBACK_STATUS_FAILED);
      item.setFailReason(sub.getCallbackFailMessage());
    }
    return item;
  }

  private Response doCallback(String url, CallbackItem item) {
    // TODO remove
    url = "http://localhost:8123/callback"; // temp for development
    Response response = httpClient.target(url).request().post(Entity.json(item));
    return response;
  }

  private boolean isOk(Response response) {
    return response != null && (response.getStatus() == Response.Status.OK.getStatusCode());
  }
}

package uk.gov.bis.lite.permissions.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.model.callback.CallbackItem;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

@Singleton
public class CallbackService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CallbackService.class);

  private static final String CALLBACK_SUCCESS = "SUCCESS";
  private static final String CALLBACK_ERROR = "ERROR";

  private final ObjectMapper objectMapper;
  private final Client httpClient;
  private OgelSubmissionDao submissionDao;

  @Inject
  public CallbackService(Client httpClient, OgelSubmissionDao submissionDao) {
    this.objectMapper = new ObjectMapper();
    this.httpClient = httpClient;
    this.submissionDao = submissionDao;
  }

  public void completeCallback(String subRef) {
    OgelSubmission sub = submissionDao.findBySubmissionRef(subRef);
    if (sub != null && sub.hasCompleted()) {
      if (doCallback(sub.getCallbackUrl(), getCallbackItem(sub))) {
        sub.setCalledBack(true);
        submissionDao.update(sub);
        LOGGER.info("CALLBACK completed [" + subRef + "]");
      } else {
        LOGGER.info("CALLBACK failed to complete [" + subRef + "]");
      }
    } else {
      LOGGER.warn("OgelSubmission has not completed its processing. Postponing callback");
    }
  }

  /*
  public void completeScheduledCallbacks() {
    List<OgelSubmission> subs = submissionDao.getScheduledCallbacks();
    LOGGER.info("CALLBACKS [" + subs.size() + "]");
    subs.stream().map(OgelSubmission::getSubmissionRef).forEach(this::completeCallback);
  }*/

  private CallbackItem getCallbackItem(OgelSubmission sub) {
    CallbackItem item = new CallbackItem();
    if (sub.isSuccess()) {
      item.setRequestId(sub.getSubmissionRef());
      item.setStatus(CALLBACK_SUCCESS);
      item.setRegistrationReference(sub.getSpireRef());
    }
    if (sub.isError()) {
      item.setRequestId(sub.getSubmissionRef());
      item.setStatus(CALLBACK_ERROR);
      item.setFailReason(sub.getFailedReason());
    }
    return item;
  }

  private boolean doCallback(String url, CallbackItem item) {
    // TODO remove
    url = "http://localhost:8123/callback"; // temp for development
    Response response = httpClient.target(url).request().post(Entity.json(item));
    return isOk(response);
  }

  private boolean isOk(Response response) {
    return response != null && (response.getStatus() == Response.Status.OK.getStatusCode());
  }
}

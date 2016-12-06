package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.util.Util;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

@Singleton
public class CallbackService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CallbackService.class);

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
    if (sub != null && sub.hasCompleted() && !sub.isCalledBack()) {
      try {
        Response response = doCallback(sub.getCallbackUrl(), getCallbackView(sub));
        if (isOk(response)) {
          sub.setCalledBack(true);
          submissionDao.update(sub);
          LOGGER.info("CALLBACK completed [" + sub.getRequestId() + "]");
        } else {
          failService.failWithMessage(sub, CallbackView.FailReason.UNCLASSIFIED, FailService.Origin.CALLBACK, Util.info(response));
        }
      } catch (ProcessingException e) {
        failService.failWithMessage(sub, CallbackView.FailReason.UNCLASSIFIED, FailService.Origin.CALLBACK, Util.info(e));
      }
    } else {
      LOGGER.warn("OgelSubmission has not completed its processing. Postponing callback");
    }
  }

  private CallbackView getCallbackView(OgelSubmission sub) {
    CallbackView view = new CallbackView();
    if (sub.isStatusSuccess()) {
      view.setRequestId(sub.getRequestId());
      view.setStatus(CallbackView.Status.SUCCESS);
      view.setRegistrationReference(sub.getSpireRef());
    }
    if (sub.isStatusError()) {
      view.setRequestId(sub.getRequestId());
      view.setStatus(CallbackView.Status.FAILED);
      CallbackView.FailReason failReason = sub.getFailReason();
      view.setFailReason(failReason);
    }
    return view;
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

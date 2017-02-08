package uk.gov.bis.lite.permissions.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.util.Util;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

@Singleton
public class CallbackServiceImpl implements CallbackService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CallbackServiceImpl.class);

  private Client httpClient;
  private FailService failService;

  @Inject
  public CallbackServiceImpl(Client httpClient, FailService failService) {
    this.httpClient = httpClient;
    this.failService = failService;
  }

  /**
   * Attempts to do callback if OgelSubmission is Status COMPLETE and is not already 'calledBack'
   */
  public boolean completeCallback(OgelSubmission sub) {
    boolean callbackCompleted = false;
    if (sub != null && sub.isStatusComplete() && !sub.isCalledBack()) {
      try {
        Response response = doCallback(sub.getCallbackUrl(), getCallbackView(sub));
        System.out.println("response: " + response.getStatus());
        if (isOk(response)) {
          sub.setCalledBack(true);
          callbackCompleted = true;
          LOGGER.info("CALLBACK completed [" + sub.getRequestId() + "]");
        } else {
          failService.failWithMessage(sub, CallbackView.FailReason.UNCLASSIFIED, FailServiceImpl.Origin.CALLBACK, Util.info(response));
        }
      } catch (ProcessingException e) {
        failService.failWithMessage(sub, CallbackView.FailReason.UNCLASSIFIED, FailServiceImpl.Origin.CALLBACK, Util.info(e));
      }
    } else {
      LOGGER.warn("OgelSubmission has not completed its processing. Postponing callback");
    }
    return callbackCompleted;
  }

  @VisibleForTesting
  public CallbackView getCallbackView(OgelSubmission sub) {
    CallbackView view = new CallbackView();
    view.setCustomerId(sub.getCustomerRef());
    view.setSiteId(sub.getSiteRef());
    if (sub.isProcessingCompleted() && sub.isCompleteSuccess()) {
      view.setRegistrationReference(sub.getSpireRef());
      view.setStatus(CallbackView.Status.SUCCESS);
    } else {
      view.setStatus(CallbackView.Status.FAILED);
    }
    view.setRequestId(sub.getRequestId());
    view.setFailReason(sub.getFailReason());
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

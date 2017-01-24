package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.api.view.OgelSubmissionView;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.List;
import java.util.stream.Collectors;

public class OgelSubmissionServiceImpl implements OgelSubmissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelSubmissionServiceImpl.class);

  private OgelSubmissionDao submissionDao;

  @Inject
  public OgelSubmissionServiceImpl(OgelSubmissionDao submissionDao) {
    this.submissionDao = submissionDao;
  }


  public boolean ogelSubmissionExists(Integer submissionId) {
    return submissionDao.findBySubmissionId(submissionId) != null;
  }

  public List<OgelSubmissionView> getOgelSubmissions() {
    return submissionDao.getScheduledCallbacks().stream().map(this::getOgelSubmissionView).collect(Collectors.toList());
  }

  public OgelSubmissionView getOgelSubmission(int submissionId) {
    return getOgelSubmissionView(submissionDao.findBySubmissionId(submissionId));
  }

  public void cancelScheduledOgelSubmissions() {
    submissionDao.getScheduledCallbacks().forEach(this::cancelScheduled);
  }

  public void cancelScheduledOgelSubmission(int submissionId) {
    cancelScheduled(submissionDao.findBySubmissionId(submissionId));
  }

  /**
   * Only SCHEDULED ogelSubmissions which have not reached the 'callback' stage can be cancelled.
   */
  private void cancelScheduled(OgelSubmission sub) {
    if (sub != null && sub.isModeScheduled() && !sub.isCalledBack()) {
      sub.changeToCancelledMode();
      submissionDao.update(sub);
    }
  }

  private OgelSubmissionView getOgelSubmissionView(OgelSubmission sub) {
    OgelSubmissionView view = new OgelSubmissionView();
    view.setId("" + sub.getId());
    view.setUserId(sub.getUserId());
    view.setOgelType(sub.getOgelType());
    view.setMode(sub.getMode().name());
    view.setStatus(sub.getStatus().name());
    view.setSubmissionRef(sub.getSubmissionRef());
    view.setCustomerRef(sub.getCustomerRef());
    view.setSiteRef(sub.getSiteRef());
    view.setSpireRef(sub.getSpireRef());
    view.setFirstFail(sub.getFirstFail());
    view.setLastFailMessage(sub.getLastFailMessage());
    if(sub.getFailReason() != null) {
      view.setFailReason(sub.getFailReason().name());
    }
    view.setCallbackUrl(sub.getCallbackUrl());
    view.setCalledBack(sub.isCalledBack());
    view.setCreated(sub.getCreated());
    view.setRoleUpdate(sub.isRoleUpdate());
    view.setRoleUpdated(sub.isRoleUpdated());
    return view;
  }

}
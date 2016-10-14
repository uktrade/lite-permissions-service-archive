package uk.gov.bis.lite.permissions.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.service.SubmissionService;

public class OgelSubmissionPrepareJob implements Job {

  private static int cycle = 0;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    SubmissionService service = (SubmissionService) context.getMergedJobDataMap()
        .get(PermissionsScheduler.SUBMISSION_SERVICE_NAME);

    if(cycle == 1) {
      service.processOgelSubmissions(OgelSubmission.Status.CUSTOMER);
    } else if(cycle == 2) {
      service.processOgelSubmissions(OgelSubmission.Status.SITE);
    } else if(cycle == 3) {
      service.processOgelSubmissions(OgelSubmission.Status.USER_ROLE);
    }
    cycle++;
    if(cycle > 3) {
      cycle = 1;
    }

  }
}

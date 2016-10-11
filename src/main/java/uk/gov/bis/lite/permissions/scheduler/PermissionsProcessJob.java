package uk.gov.bis.lite.permissions.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.model.OgelRegistration;
import uk.gov.bis.lite.permissions.service.RegistrationService;

public class PermissionsProcessJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsProcessJob.class);
  private static int cycle = 0;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    RegistrationService service = (RegistrationService) context.getMergedJobDataMap()
        .get(PermissionsScheduler.REG_SERVICE_NAME);

    if(cycle == 1) {
      service.processRegistrations(OgelRegistration.Status.CUSTOMER);
    } else if(cycle == 2) {
      service.processRegistrations(OgelRegistration.Status.SITE);
    } else if(cycle == 3) {
      service.processRegistrations(OgelRegistration.Status.SITE_PERMISSION);
    } else if(cycle == 4) {
      service.processRegistrations(OgelRegistration.Status.PENDING);
    }
    cycle++;
    if(cycle > 4) {
      cycle = 1;
    }

  }
}

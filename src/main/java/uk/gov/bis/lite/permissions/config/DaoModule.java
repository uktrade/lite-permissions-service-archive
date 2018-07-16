package uk.gov.bis.lite.permissions.config;

import com.google.inject.AbstractModule;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDao;
import uk.gov.bis.lite.permissions.dao.OgelSubmissionDaoImpl;

public class DaoModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(OgelSubmissionDao.class).to(OgelSubmissionDaoImpl.class);
  }

}

package uk.gov.bis.lite.permissions.dao.sqlite;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.List;

public interface OgelSubmissionInterface {

  @SqlUpdate("UPDATE LOCAL_OGEL_SUBMISSION SET CUSTOMER_REF = :customerRef, SITE_REF = :siteRef, SPIRE_REF = :spireRef, " +
      "MODE = :mode, STATUS = :status, ROLE_UPDATED = :roleUpdated, CALLED_BACK = :calledBack, FIRST_FAIL = :firstFail, " +
      " LAST_FAIL_MESSAGE = :lastFailMessage WHERE id = :id")
  void update(@Bind("customerRef") String customerRef,
              @Bind("siteRef") String siteRef,
              @Bind("spireRef") String spireRef,
              @Bind("mode") String mode,
              @Bind("status") String status,
              @Bind("roleUpdated") Boolean roleUpdated,
              @Bind("calledBack") Boolean calledBack,
              @Bind("firstFail") String firstFail,
              @Bind("lastFailMessage") String lastFailMessage,
              @Bind("id") int id);

  @SqlUpdate("INSERT INTO LOCAL_OGEL_SUBMISSION (USER_ID, OGEL_TYPE, SUBMISSION_REF, CUSTOMER_REF, SITE_REF, SPIRE_REF, " +
      "CALLBACK_URL, CALLED_BACK, JSON, MODE, STATUS, ROLE_UPDATE, ROLE_UPDATED) VALUES (:userId, :ogelType, :submissionRef, :customerRef, :siteRef, " +
      " :spireRef, :callbackUrl, :calledBack, :json, :mode, :status, :roleUpdate, :roleUpdated)")
  void insert(@Bind("userId") String userId,
              @Bind("ogelType") String ogelType,
              @Bind("submissionRef") String submissionRef,
              @Bind("customerRef") String customerRef,
              @Bind("siteRef") String siteRef,
              @Bind("spireRef") String spireRef,
              @Bind("callbackUrl") String callbackUrl,
              @Bind("calledBack") Boolean calledBack,
              @Bind("json") String json,
              @Bind("mode") String mode,
              @Bind("status") String status,
              @Bind("roleUpdate") Boolean roleUpdate,
              @Bind("roleUpdated") Boolean roleUpdated);


  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE MODE = 'SCHEDULED' AND !(STATUS = 'SUCCESS' || STATUS = 'ERROR')")
  @Mapper(OgelSubmissionMapper.class)
  List<OgelSubmission> getScheduled();

  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE STATUS = :status AND MODE = 'SCHEDULED'")
  @Mapper(OgelSubmissionMapper.class)
  List<OgelSubmission> getScheduledByStatus(@Bind("status") String status);

  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE (STATUS = 'SUCCESS' || STATUS = 'ERROR') " +
      "  AND MODE = 'SCHEDULED' AND (CALLBACK_URL IS NOT NULL AND CALLBACK_URL != '') AND CALLED_BACK = 0")
  @Mapper(OgelSubmissionMapper.class)
  List<OgelSubmission> getScheduledCallbacks();

  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE SUBMISSION_REF = :submissionRef")
  @Mapper(OgelSubmissionMapper.class)
  OgelSubmission findBySubmissionRef(@Bind("submissionRef") String submissionRef);

  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE SUBMISSION_REF = :submissionRef AND CREATED > date('now','-3 months')")
  @Mapper(OgelSubmissionMapper.class)
  OgelSubmission findRecentBySubmissionRef(@Bind("submissionRef") String submissionRef);

}

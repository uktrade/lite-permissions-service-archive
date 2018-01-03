package uk.gov.bis.lite.permissions.dao.sqlite;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.time.LocalDateTime;
import java.util.List;

public interface OgelSubmissionInterface {

  @SqlUpdate("UPDATE LOCAL_OGEL_SUBMISSION SET CUSTOMER_REF = :customerRef, SITE_REF = :siteRef, SPIRE_REF = :spireRef, " +
      "MODE = :mode, STATUS = :status, STAGE = :stage, ROLE_UPDATED = :roleUpdated, CALLED_BACK = :calledBack, FIRST_FAIL = :firstFail, LAST_FAIL = :lastFail, " +
      " LAST_FAIL_MESSAGE = :lastFailMessage, FAIL_REASON = :failReason, CALLBACK_FAIL_COUNT = :callbackFailCount, LITE_JWT_USER = :liteJwtUser WHERE id = :id")
  void update(@Bind("customerRef") String customerRef,
              @Bind("siteRef") String siteRef,
              @Bind("spireRef") String spireRef,
              @Bind("mode") String mode,
              @Bind("status") String status,
              @Bind("stage") String stage,
              @Bind("roleUpdated") Boolean roleUpdated,
              @Bind("calledBack") Boolean calledBack,
              @Bind("firstFail") LocalDateTime firstFail,
              @Bind("lastFail") LocalDateTime lastFail,
              @Bind("lastFailMessage") String lastFailMessage,
              @Bind("failReason") String failReason,
              @Bind("callbackFailCount") int callbackFailCount,
              @Bind("liteJwtUser") String liteJwtUser,
              @Bind("id") int id);

  @SqlUpdate("INSERT INTO LOCAL_OGEL_SUBMISSION (USER_ID, OGEL_TYPE, SUBMISSION_REF, CUSTOMER_REF, SITE_REF, SPIRE_REF, " +
      "CALLBACK_URL, CALLED_BACK, JSON, MODE, STATUS, STAGE, ROLE_UPDATE, ROLE_UPDATED, ADMIN_USER_ID, CALLBACK_FAIL_COUNT, LITE_JWT_USER) VALUES (:userId, :ogelType, :submissionRef, :customerRef, :siteRef, " +
      " :spireRef, :callbackUrl, :calledBack, :json, :mode, :status, :stage, :roleUpdate, :roleUpdated, :adminUserId, :callbackFailCount, :liteJwtUser)")
  @GetGeneratedKeys
  int insert(@Bind("userId") String userId,
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
             @Bind("stage") String stage,
             @Bind("roleUpdate") Boolean roleUpdate,
             @Bind("roleUpdated") Boolean roleUpdated,
             @Bind("adminUserId") String adminUserId,
             @Bind("callbackFailCount") int callbackFailCount,
             @Bind("liteJwtUser") String liteJwtUser);

  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE MODE = 'SCHEDULED' AND STATUS = 'ACTIVE'")
  @Mapper(OgelSubmissionMapper.class)
  List<OgelSubmission> getScheduledActive();

  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE MODE = 'SCHEDULED' AND STATUS = 'COMPLETE' AND CALLED_BACK = FALSE")
  @Mapper(OgelSubmissionMapper.class)
  List<OgelSubmission> getScheduledCompleteToCallback();

  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE (STATUS = 'ACTIVE' OR STATUS = 'COMPLETE') AND CALLED_BACK = FALSE ORDER BY ID DESC")
  @Mapper(OgelSubmissionMapper.class)
  List<OgelSubmission> getPendingSubmissions();

  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE (STATUS = 'CANCELLED' OR STATUS = 'TERMINATED') ORDER BY ID DESC")
  @Mapper(OgelSubmissionMapper.class)
  List<OgelSubmission> getCancelledSubmissions();

  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE STATUS = 'COMPLETE' AND CALLED_BACK = TRUE ORDER BY ID DESC")
  @Mapper(OgelSubmissionMapper.class)
  List<OgelSubmission> getFinishedSubmissions();

  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE ID = :submissionId")
  @Mapper(OgelSubmissionMapper.class)
  OgelSubmission findBySubmissionId(@Bind("submissionId") int submissionId);

  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE SUBMISSION_REF = :submissionRef AND STATUS = 'ACTIVE' OR (STATUS = 'COMPLETE' AND CALLED_BACK = FALSE)")
  @Mapper(OgelSubmissionMapper.class)
  OgelSubmission findRecentBySubmissionRef(@Bind("submissionRef") String submissionRef);

  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE SUBMISSION_REF = :submissionRef")
  @Mapper(OgelSubmissionMapper.class)
  OgelSubmission findBySubmissionRef(@Bind("submissionRef") String submissionRef);
}

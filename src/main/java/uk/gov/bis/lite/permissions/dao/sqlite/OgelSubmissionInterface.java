package uk.gov.bis.lite.permissions.dao.sqlite;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.util.List;

public interface OgelSubmissionInterface {


  @SqlUpdate("UPDATE LOCAL_OGEL_SUBMISSION SET CUSTOMER_REF = :customerRef, SITE_REF = :siteRef, SPIRE_REF = :spireRef, " +
      "STATUS = :status WHERE id = :id")
  void update(@Bind("customerRef") String customerRef,
              @Bind("siteRef") String siteRef,
              @Bind("spireRef") String spireRef,
              @Bind("status") String status,
              @Bind("id") int id);

  @SqlUpdate("INSERT INTO LOCAL_OGEL_SUBMISSION (USER_ID, OGEL_TYPE, SUBMISSION_REF, CUSTOMER_REF, SITE_REF, SPIRE_REF, JSON, STATUS, ROLE_UPDATE) " +
      "VALUES (:userId, :ogelType, :submissionRef, :customerRef, :siteRef, :spireRef, :json, :status, :roleUpdate)")
  void insert(@Bind("userId") String userId,
              @Bind("ogelType") String ogelType,
              @Bind("submissionRef") String submissionRef,
              @Bind("customerRef") String customerRef,
              @Bind("siteRef") String siteRef,
              @Bind("spireRef") String spireRef,
              @Bind("json") String json,
              @Bind("status") String status,
              @Bind("roleUpdate") Boolean roleUpdate);


  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE STATUS = :status")
  @Mapper(OgelSubmissionMapper.class)
  List<OgelSubmission> getByStatus(@Bind("status") String status);


  @SqlQuery("SELECT * FROM LOCAL_OGEL_SUBMISSION WHERE SUBMISSION_REF = :submissionRef")
  @Mapper(OgelSubmissionMapper.class)
  OgelSubmission findBySubmissionRef(@Bind("submissionRef") String submissionRef);

}

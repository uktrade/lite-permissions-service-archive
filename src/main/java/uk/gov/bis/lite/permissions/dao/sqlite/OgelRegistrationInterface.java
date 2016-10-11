package uk.gov.bis.lite.permissions.dao.sqlite;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import uk.gov.bis.lite.permissions.model.OgelRegistration;

import java.util.List;

public interface OgelRegistrationInterface {


  @SqlUpdate("UPDATE LOCAL_OGEL_REGISTRATION SET CUSTOMER_ID = :customerId, SITE_ID = :siteId, STATUS = :status " +
      "WHERE id = :id")
  void update(@Bind("customerId") String customerId,
              @Bind("siteId") String siteId,
              @Bind("status") String status,
              @Bind("id") int id);

  @SqlUpdate("INSERT INTO LOCAL_OGEL_REGISTRATION (USER_ID, OGEL_TYPE, LITE_ID, CUSTOMER_ID, SITE_ID, JSON, STATUS) " +
      "VALUES (:userId, :ogelType, :liteId, :customerId, :siteId, :json, :status)")
  void insert(@Bind("userId") String userId,
              @Bind("ogelType") String ogelType,
              @Bind("liteId") String liteId,
              @Bind("customerId") String customerId,
              @Bind("siteId") String siteId,
              @Bind("json") String json,
              @Bind("status") String status);


  @SqlQuery("SELECT * FROM LOCAL_OGEL_REGISTRATION WHERE STATUS = :status")
  @Mapper(OgelRegistrationMapper.class)
  List<OgelRegistration> getByStatus(@Bind("status") String status);


  @SqlQuery("SELECT * FROM LOCAL_OGEL_REGISTRATION WHERE LITE_ID = :liteId")
  @Mapper(OgelRegistrationMapper.class)
  OgelRegistration findByLiteId(@Bind("liteId") String liteId);

}

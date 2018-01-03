package uk.gov.bis.lite.permissions.dao.sqlite;

import org.apache.commons.lang3.EnumUtils;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.permissions.model.OgelSubmission;
import uk.gov.bis.lite.permissions.util.LiteJwtUserUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class OgelSubmissionMapper implements ResultSetMapper<OgelSubmission> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OgelSubmissionMapper.class);

  @Override
  public OgelSubmission map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    OgelSubmission sub = new OgelSubmission(r.getInt("ID"));
    sub.setUserId(r.getString("USER_ID"));
    sub.setAdminUserId(r.getString("ADMIN_USER_ID"));
    sub.setOgelType(r.getString("OGEL_TYPE"));
    sub.setSubmissionRef(r.getString("SUBMISSION_REF"));
    sub.setCustomerRef(r.getString("CUSTOMER_REF"));
    sub.setMode(OgelSubmission.Mode.valueOf(r.getString("MODE")));
    sub.setStatus(OgelSubmission.Status.valueOf(r.getString("STATUS")));
    sub.setStage(OgelSubmission.Stage.valueOf(r.getString("STAGE")));
    sub.setSiteRef(r.getString("SITE_REF"));
    sub.setSpireRef(r.getString("SPIRE_REF"));
    sub.setCallbackUrl(r.getString("CALLBACK_URL"));
    sub.setCalledBack(r.getBoolean("CALLED_BACK"));
    sub.setFirstFail(r.getObject("FIRST_FAIL", LocalDateTime.class));
    sub.setLastFail(r.getObject("LAST_FAIL", LocalDateTime.class));
    sub.setLastFailMessage(r.getString("LAST_FAIL_MESSAGE"));
    sub.setCallBackFailCount(r.getInt("CALLBACK_FAIL_COUNT"));
    sub.setLiteJwtUser(LiteJwtUserUtil.fromJson(r.getString("LITE_JWT_USER")));

    if (r.getString("FAIL_REASON") == null) {
      sub.setFailReason(null);
    } else {
      String failReasonValue = r.getString("FAIL_REASON");
      if (EnumUtils.isValidEnum(OgelSubmission.FailReason.class, failReasonValue)) {
        sub.setFailReason(OgelSubmission.FailReason.valueOf(failReasonValue));
      } else {
        LOGGER.warn("Database FailReason is not valid for Enum: {}", failReasonValue);
        sub.setFailReason(null);
      }
    }

    sub.setJson(r.getString("JSON"));
    sub.setCreated(r.getObject("CREATED", LocalDateTime.class));
    sub.setRoleUpdate(r.getBoolean("ROLE_UPDATE"));
    sub.setRoleUpdated(r.getBoolean("ROLE_UPDATED"));
    return sub;
  }
}

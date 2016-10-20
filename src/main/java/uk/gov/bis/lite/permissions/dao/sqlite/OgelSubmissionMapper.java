package uk.gov.bis.lite.permissions.dao.sqlite;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.bis.lite.permissions.model.OgelSubmission;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OgelSubmissionMapper implements ResultSetMapper<OgelSubmission> {

  @Override
  public OgelSubmission map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    OgelSubmission sub = new OgelSubmission(r.getInt("ID"));
    sub.setUserId(r.getString("USER_ID"));
    sub.setOgelType(r.getString("OGEL_TYPE"));
    sub.setSubmissionRef(r.getString("SUBMISSION_REF"));
    sub.setCustomerRef(r.getString("CUSTOMER_REF"));
    sub.setMode(OgelSubmission.Mode.valueOf(r.getString("MODE")));
    sub.setStatus(OgelSubmission.Status.valueOf(r.getString("STATUS")));
    sub.setSiteRef(r.getString("SITE_REF"));
    sub.setSpireRef(r.getString("SPIRE_REF"));
    sub.setCallbackUrl(r.getString("CALLBACK_URL"));
    sub.setCalledBack(r.getBoolean("CALLED_BACK"));
    sub.setFirstFail(r.getString("FIRST_FAIL"));
    sub.setLastFailMessage(r.getString("LAST_FAIL_MESSAGE"));
    sub.setJson(r.getString("JSON"));
    sub.setCreated(r.getString("CREATED"));
    sub.setRoleUpdate(r.getBoolean("ROLE_UPDATE"));
    sub.setRoleUpdated(r.getBoolean("ROLE_UPDATED"));
    return sub;
  }
}

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
    sub.setStatus(OgelSubmission.Status.valueOf(r.getString("STATUS")));
    sub.setSiteRef(r.getString("SITE_REF"));
    sub.setSpireRef(r.getString("SPIRE_REF"));
    sub.setJson(r.getString("JSON"));
    sub.setCreated(r.getString("CREATED"));
    sub.setRoleUpdate(r.getBoolean("ROLE_UPDATE"));
    return sub;
  }
}

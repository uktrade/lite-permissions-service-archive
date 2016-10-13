package uk.gov.bis.lite.permissions.dao.sqlite;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.bis.lite.permissions.model.OgelRegistration;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OgelRegistrationMapper implements ResultSetMapper<OgelRegistration> {

  @Override
  public OgelRegistration map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    OgelRegistration ogReg = new OgelRegistration(r.getInt("ID"));
    ogReg.setUserId(r.getString("USER_ID"));
    ogReg.setOgelType(r.getString("OGEL_TYPE"));
    ogReg.setLiteId(r.getString("LITE_ID"));
    ogReg.setCustomerId(r.getString("CUSTOMER_ID"));
    ogReg.setStatus(OgelRegistration.Status.valueOf(r.getString("STATUS")));
    ogReg.setSiteId(r.getString("SITE_ID"));
    ogReg.setJson(r.getString("JSON"));
    ogReg.setCreated(r.getString("CREATED"));
    ogReg.setRoleUpdate(r.getBoolean("ROLE_UPDATE"));
    return ogReg;
  }
}

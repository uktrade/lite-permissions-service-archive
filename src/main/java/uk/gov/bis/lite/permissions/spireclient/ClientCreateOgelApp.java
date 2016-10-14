package uk.gov.bis.lite.permissions.spireclient;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.xml.soap.SOAPMessage;

public class ClientCreateOgelApp extends SpireClient {

  private static final String VERSION_NO = "1.0";
  private static final String NAMESPACE = "SPIRE_CREATE_OGEL_APP";
  private static final String CHILD_NAME = "OGEL_DETAILS";

  @Inject
  public ClientCreateOgelApp(@Named("spireCreateOgelAppUrl") String url,
                             @Named("soapUserName") String userName,
                             @Named("soapPassword") String password) {
    super(url, userName, password);
  }

  public SOAPMessage createOgelApp(String userId, String sarRef, String siteRef, String ogelType) {
    SOAPMessage message = getRequest(NAMESPACE, CHILD_NAME);
    addChild(message, "VERSION_NO", VERSION_NO);
    addChild(message, "WUA_ID", userId);
    addChild(message, "SAR_REF", sarRef);
    addChild(message, "SITE_REF", siteRef);
    addChildList(message, "OGL_TYPE_LIST", "OGL_TYPE", "TYPE", ogelType);
    return getResponse(message);
  }
}

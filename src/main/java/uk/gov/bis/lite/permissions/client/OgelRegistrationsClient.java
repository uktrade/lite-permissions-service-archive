package uk.gov.bis.lite.permissions.client;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.xml.soap.SOAPMessage;

public class OgelRegistrationsClient extends SpireClient {

  private static final String ENVELOPE_NAMESPACE = "SPIRE_OGEL_REGISTRATIONS";
  private static final String CHECK_OGEL_STATUS = "checkOgelStatus";
  private static final String GET_OGEL_REGS = "getOgelRegs";

  @Inject
  public OgelRegistrationsClient(@Named("spireOgelRegistrationsUrl") String url,
                                 @Named("soapUserName") String userName,
                                 @Named("soapPassword") String password) {
    super(url, userName, password);
  }

  /**
   * userId, sarRef, siteRef, ogelRef
   */
  public SOAPMessage checkOgelStatus(String userId) {
    SOAPMessage request = getRequest(ENVELOPE_NAMESPACE, CHECK_OGEL_STATUS);
    addChild(request, "userId", userId);
    return getResponse(request);
  }
}

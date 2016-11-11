package uk.gov.bis.lite.permissions.spire.parsers;


import org.w3c.dom.Node;
import uk.gov.bis.lite.common.spire.client.SpireResponse;
import uk.gov.bis.lite.common.spire.client.parser.SpireParser;
import uk.gov.bis.lite.permissions.spire.model.SpireOgelRegistration;

import java.util.ArrayList;
import java.util.List;

public class OgelRegistrationParser implements SpireParser<List<SpireOgelRegistration>> {

  @Override
  public List<SpireOgelRegistration> parseResponse(SpireResponse spireResponse) {
    return getSitesFromNodes(spireResponse.getElementChildNodesForList("//OGEL_REGISTRATION_LIST"));
  }

  private List<SpireOgelRegistration> getSitesFromNodes(List<Node> nodes) {
    List<SpireOgelRegistration> regs = new ArrayList<>();
    for (Node node : nodes) {
      SpireOgelRegistration reg = new SpireOgelRegistration();
      SpireResponse.getNodeValue(node, "SAR_REF").ifPresent(reg::setSarRef);
      SpireResponse.getNodeValue(node, "SITE_REF").ifPresent(reg::setSiteRef);
      SpireResponse.getNodeValue(node, "OGEL_TYPE_REF").ifPresent(reg::setOgelTypeRef);
      SpireResponse.getNodeValue(node, "REGISTRATION_REF").ifPresent(reg::setRegistrationRef);
      SpireResponse.getNodeValue(node, "REGISTRATION_DATE").ifPresent(reg::setRegistrationDate);
      SpireResponse.getNodeValue(node, "STATUS").ifPresent(reg::setStatus);
      regs.add(reg);
    }
    return regs;
  }

}


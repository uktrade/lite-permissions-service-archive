package uk.gov.bis.lite.spireclient;

import uk.gov.bis.lite.spireclient.model.SpireRequest;
import uk.gov.bis.lite.spireclient.model.SpireResponse;

public interface SpireClient {

  SpireRequest getSpireRequest(SpireClientService.Endpoint endpoint, String userId);

  SpireResponse executeRequest(SpireRequest request);

}

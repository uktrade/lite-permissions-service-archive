package uk.gov.bis.lite.permissions.spire.clients;


import uk.gov.bis.lite.common.spire.client.SpireClient;
import uk.gov.bis.lite.common.spire.client.SpireClientConfig;
import uk.gov.bis.lite.common.spire.client.SpireRequestConfig;
import uk.gov.bis.lite.common.spire.client.errorhandler.ErrorHandler;
import uk.gov.bis.lite.common.spire.client.parser.SpireParser;
import uk.gov.bis.lite.permissions.spire.model.SpireOgelRegistration;

import java.util.List;

public class SpireOgelRegistrationClient extends SpireClient<List<SpireOgelRegistration>> {

  public SpireOgelRegistrationClient(SpireParser<List<SpireOgelRegistration>> parser,
                                     SpireClientConfig clientConfig,
                                     SpireRequestConfig requestConfig,
                                     ErrorHandler errorHandler) {
    super(parser, clientConfig, requestConfig, errorHandler);
  }
}


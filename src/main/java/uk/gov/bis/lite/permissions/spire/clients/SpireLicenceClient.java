package uk.gov.bis.lite.permissions.spire.clients;

import uk.gov.bis.lite.common.spire.client.SpireClient;
import uk.gov.bis.lite.common.spire.client.SpireClientConfig;
import uk.gov.bis.lite.common.spire.client.SpireRequestConfig;
import uk.gov.bis.lite.common.spire.client.errorhandler.ErrorHandler;
import uk.gov.bis.lite.common.spire.client.parser.SpireParser;
import uk.gov.bis.lite.permissions.spire.model.SpireLicence;

import java.util.List;

public class SpireLicenceClient extends SpireClient<List<SpireLicence>> {
  public SpireLicenceClient(SpireParser<List<SpireLicence>> parser, SpireClientConfig clientConfig, SpireRequestConfig requestConfig, ErrorHandler errorHandler) {
    super(parser, clientConfig, requestConfig, errorHandler);
  }
}

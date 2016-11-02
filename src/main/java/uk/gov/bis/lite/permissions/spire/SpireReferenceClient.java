package uk.gov.bis.lite.permissions.spire;

import uk.gov.bis.lite.spire.client.SpireClient;
import uk.gov.bis.lite.spire.client.SpireClientConfig;
import uk.gov.bis.lite.spire.client.SpireRequestConfig;
import uk.gov.bis.lite.spire.client.parser.SpireParser;

public class SpireReferenceClient extends SpireClient<String> {

  public SpireReferenceClient(SpireParser<String> parser,
                              SpireClientConfig clientConfig,
                              SpireRequestConfig requestConfig) {
    super(parser, clientConfig, requestConfig);
  }
}

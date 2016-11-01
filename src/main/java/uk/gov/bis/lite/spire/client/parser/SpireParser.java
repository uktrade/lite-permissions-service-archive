package uk.gov.bis.lite.spire.client.parser;


import uk.gov.bis.lite.spire.client.SpireResponse;

public interface SpireParser<T> {

  T parseResponse(SpireResponse spireResponse);

}

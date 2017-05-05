package uk.gov.bis.lite.permissions.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

public class LoggingFilter implements ClientRequestFilter, ClientResponseFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

  public LoggingFilter() {}

  public void filter(ClientRequestContext requestContext) throws IOException {
    String method = requestContext.getMethod();
    String uri = requestContext.getUri().toString();
    LOGGER.info("Request {} {}", method, uri);
  }

  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
    int statusCode = responseContext.getStatus();
    String statusReason = responseContext.getStatusInfo().getReasonPhrase();
    LOGGER.info("Response {} {}", statusCode, statusReason);
  }
}


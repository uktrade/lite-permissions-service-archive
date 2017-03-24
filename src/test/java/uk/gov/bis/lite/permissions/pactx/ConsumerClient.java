package uk.gov.bis.lite.permissions.pactx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.UrlEscapers;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsumerClient {

  private String url;

  public ConsumerClient(String url) {
    this.url = url;
  }

  public int post(String path) throws IOException {
    return Request.Post(url + encodePath(path))
        .execute().returnResponse().getStatusLine().getStatusCode();
  }

  public int postWithBody(String path, String body, ContentType mimeType) throws IOException {
    return Request.Post(url + encodePath(path))
        .bodyString(body, mimeType)
        .execute().returnResponse().getStatusLine().getStatusCode();
  }

  public String getAsJsonString(String path) throws IOException {
    URIBuilder uriBuilder;
    try {
      uriBuilder = new URIBuilder(url).setPath(path);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return Request.Get(uriBuilder.toString()).execute().returnContent().asString();
  }

  public String getAsJsonString(String path, String queryString) throws IOException {
    URIBuilder uriBuilder;
    try {
      uriBuilder = new URIBuilder(url).setPath(path);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    if (StringUtils.isNotEmpty(queryString)) {
      uriBuilder.setParameters(parseQueryString(queryString));
    }
    return Request.Get(uriBuilder.toString()).execute().returnContent().asString();
  }

  public Map getAsMap(String path, String queryString) throws IOException {
    URIBuilder uriBuilder;
    try {
      uriBuilder = new URIBuilder(url).setPath(path);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    if (StringUtils.isNotEmpty(queryString)) {
      uriBuilder.setParameters(parseQueryString(queryString));
    }
    return jsonToMap(Request.Get(uriBuilder.toString())
        .execute().returnContent().asString());
  }

  public int options(String path) throws IOException {
    return Request.Options(url + encodePath(path))
        .execute().returnResponse().getStatusLine().getStatusCode();
  }

  public String postBody(String path, String body, ContentType mimeType) throws IOException {
    return Request.Post(url + encodePath(path))
        .bodyString(body, mimeType)
        .execute().returnContent().asString();
  }

  public Map putAsMap(String path, String body) throws IOException {
    String respBody = Request.Put(url + encodePath(path))
        .bodyString(body, ContentType.APPLICATION_JSON)
        .execute().returnContent().asString();
    return jsonToMap(respBody);
  }

  public List getAsList(String path) throws IOException {
    return jsonToList(Request.Get(url + encodePath(path))
        .execute().returnContent().asString());
  }

  public Map post(String path, String body, ContentType mimeType) throws IOException {
    String respBody = Request.Post(url + encodePath(path))
        .bodyString(body, mimeType)
        .execute().returnContent().asString();
    return jsonToMap(respBody);
  }

  private List<NameValuePair> parseQueryString(String queryString) {
    return Arrays.asList(queryString.split("&")).stream().map(s -> s.split("="))
        .map(p -> new BasicNameValuePair(p[0], p[1])).collect(Collectors.toList());
  }

  private String encodePath(String path) {
    return Arrays.asList(path.split("/"))
        .stream().map(UrlEscapers.urlPathSegmentEscaper()::escape).collect(Collectors.joining("/"));
  }

  private HashMap jsonToMap(String respBody) throws IOException {
    if (respBody.isEmpty()) {
      return new HashMap();
    }
    return new ObjectMapper().readValue(respBody, HashMap.class);
  }

  private List jsonToList(String respBody) throws IOException {
    return new ObjectMapper().readValue(respBody, ArrayList.class);
  }
}
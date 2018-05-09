package uk.gov.bis.lite.permissions;

import com.google.common.collect.ImmutableMap;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;

import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

public class JwtTestHelper {
  /*
    {
      "typ": "JWT",
      "alg": "HS256"
    }
    {
      "iss": "Some lite application",
      "exp": 1825508319,
      "jti": "Jv1XdmhlFhrbQhq5QaPgyg",
      "iat": 1510148319,
      "nbf": 1510148199,
      "sub": "123456",
      "email": "example@example.com",
      "fullName": "Mr Test",
      "accountType": "UNKNOWN"
    }
    Secret: demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement
  */
  public static final String JWT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTb21lIGxpdGUgYXBwbGljYXRpb24iLCJleHAiOjE4MjU1MDgzMTksImp0aSI6Ikp2MVhkbWhsRmhyYlFocTVRYVBneWciLCJpYXQiOjE1MTAxNDgzMTksIm5iZiI6MTUxMDE0ODE5OSwic3ViIjoiMTIzNDU2IiwiZW1haWwiOiJleGFtcGxlQGV4YW1wbGUuY29tIiwiZnVsbE5hbWUiOiJNciBUZXN0IiwiYWNjb3VudFR5cGUiOiJVTktOT1dOIn0.dSmBA7gcZwcDiRFH4a8IjHjFQK54bXVRckl5i9tK6mk";

  public static final String JWT_AUTHORIZATION_HEADER_VALUE = "Bearer " + JWT_TOKEN;

  public static final Map<String, String> JWT_AUTHORIZATION_HEADER =
      ImmutableMap.of(HttpHeaders.AUTHORIZATION, JWT_AUTHORIZATION_HEADER_VALUE);

  public static final String JWT_AUTHORIZATION_HEADER_REGEX = "^Bearer [A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]*$";

  public static final String JWT_SHARED_SECRET = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";

  public static final LiteJwtUser LITE_JWT_USER = new LiteJwtUser()
      .setUserId("123456")
      .setEmail("example@example.com")
      .setFullName("Mr Test");
}

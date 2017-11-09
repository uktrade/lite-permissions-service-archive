package uk.gov.bis.lite.permissions.spire.errorhandlers;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Before;
import org.junit.Test;
import uk.gov.bis.lite.common.spire.client.SpireResponse;
import uk.gov.bis.lite.common.spire.client.errorhandler.ErrorNodeErrorHandler;
import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;
import uk.gov.bis.lite.permissions.spire.exceptions.SpireUserNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

public class OgelRegistrationErrorHandlerTest {
  private ErrorNodeErrorHandler errorHandler;

  @Before
  public void setUp() throws Exception {
    errorHandler = new OgelRegistrationErrorHandler();
  }

  static SpireResponse createSpireResponse(String soapMessageString) throws Exception {
    InputStream is = new ByteArrayInputStream(soapMessageString.getBytes());
    SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
    return new SpireResponse(soapMessage);
  }

  @Test
  public void userIdDoesNotExistTest() throws Exception {
    String errorText = "Web user account for provided userId not found.";
    assertThatThrownBy(() -> errorHandler.handleError(errorText))
        .isInstanceOf(SpireUserNotFoundException.class)
        .hasMessageContaining("User not found: \"Web user account for provided userId not found.\"");
  }

  @Test
  public void userIdDoesNotExistResponseTest() throws Exception {
    SpireResponse response = createSpireResponse(fixture("fixture/soap/SPIRE_OGEL_REGISTRATIONS/userIdDoesNotExist.xml"));
    assertThatThrownBy(() -> errorHandler.checkResponse(response))
        .isInstanceOf(SpireUserNotFoundException.class)
        .hasMessageContaining("User not found: \"Web user account for provided userId not found.\"");
  }

  @Test
  public void unhandledErrorTest() throws Exception {
    String errorText = "Some other error.";
    assertThatThrownBy(() -> errorHandler.handleError(errorText))
        .isInstanceOf(SpireClientException.class)
        .hasMessageContaining("Unhandled error: \"Some other error.\"");
  }

  @Test
  public void unhandledErrorResponseTest() throws Exception {
    SpireResponse response = createSpireResponse(fixture("fixture/soap/SPIRE_OGEL_REGISTRATIONS/unhandledError.xml"));

    assertThatThrownBy(() -> errorHandler.checkResponse(response))
        .isInstanceOf(SpireClientException.class)
        .hasMessageContaining("Unhandled error: \"Some other error.\"");
  }

  @Test
  public void noErrorTest() throws Exception {
    SpireResponse response = createSpireResponse(fixture("fixture/soap/SPIRE_OGEL_REGISTRATIONS/ogelRegistrations.xml"));

    errorHandler.checkResponse(response);
  }
}
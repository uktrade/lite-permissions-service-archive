package uk.gov.bis.lite.permissions.spire;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import uk.gov.bis.lite.common.spire.client.SpireResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * Base class for spire parsing tests
 */
class SpireParseTest {

  SpireResponse createSpireResponse(String fileName) {
    return new SpireResponse(getSoapMessage(fileName));
  }

  private SOAPMessage getSoapMessage(String fileName) {
    return createSoapMessage(readResource(fileName, Charsets.UTF_8));
  }

  private String readResource(String fileName, Charset charset) {
    String fileContent = "";
    try {
      fileContent = Resources.toString(Resources.getResource(fileName), charset);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return fileContent;
  }

  private SOAPMessage createSoapMessage(String xml) {
    SOAPMessage message = null;
    try {
      message = MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(xml.getBytes()));
    } catch (IOException | SOAPException e) {
      e.printStackTrace();
    }
    return message;
  }

}
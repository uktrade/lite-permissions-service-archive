package uk.gov.bis.lite.spireclient.model;

public class SpireResponse {

  private String ref;
  private String errorMessage;

  public static SpireResponse error(String message){
    SpireResponse response = new SpireResponse();
    response.setErrorMessage(message);
    return response;
  }

  public String getInfo() {
    String refInfo = ref == null ? "ref is null" : ref;
    String errorMessageInfo = errorMessage == null ? "errorMessage is null" : errorMessage;

    return refInfo + " - " + errorMessageInfo;
  }

  public boolean hasError() {
    return errorMessage != null;
  }

  public boolean hasRef() {
    return ref != null;
  }

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}

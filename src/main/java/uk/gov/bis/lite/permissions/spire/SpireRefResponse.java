package uk.gov.bis.lite.permissions.spire;

public class SpireRefResponse {

  private String ref;
  private String errorMessage;

  public static SpireRefResponse error(String message){
    SpireRefResponse response = new SpireRefResponse();
    response.setErrorMessage(message);
    return response;
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

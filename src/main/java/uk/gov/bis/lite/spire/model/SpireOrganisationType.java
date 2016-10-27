package uk.gov.bis.lite.spire.model;

public enum SpireOrganisationType {

  TC("Trading Company"), O("Organisation");

  private String typeLongName;

  SpireOrganisationType(String typeLongName) {
    this.typeLongName = typeLongName;
  }

  public String getTypeLongName() {
    return typeLongName;
  }
}

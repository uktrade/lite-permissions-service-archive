package uk.gov.bis.lite.common.item.out;

import java.util.List;

public class UsersOut {

  private List<UserOut> administrators;

  public UsersOut() {
  }

  public UsersOut(List<UserOut> administrators) {
    this.administrators = administrators;
  }


  public List<UserOut> getAdministrators() {
    return administrators;
  }

  public void setAdministrators(List<UserOut> administrators) {
    this.administrators = administrators;
  }
}

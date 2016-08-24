package net.dankito.jpa.couchbaselite.testmodel.inheritance;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Created by ganymed on 24/08/16.
 */
@MappedSuperclass
public class JoinTableChild_2_MappedSuperclass extends JoinedTableBase {

  public static final String GIVEN_NAME_COLUMN_NAME = "given_name";


  @Column(name = GIVEN_NAME_COLUMN_NAME)
  protected String givenName;


  public JoinTableChild_2_MappedSuperclass() {

  }

  public JoinTableChild_2_MappedSuperclass(String name, String givenName) {
    super(name);
    this.givenName = givenName;
  }


  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

}

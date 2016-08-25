package net.dankito.jpa.couchbaselite.testmodel.inheritance;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by ganymed on 24/08/16.
 */
@Entity
@DiscriminatorValue(value = "2")
public class SingleTableChild_2 extends SingleTableBase {

  @Column
  protected String city;


  public SingleTableChild_2() {

  }

  public SingleTableChild_2(String name, String city) {
    super(name);
    this.city = city;
  }


  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

}

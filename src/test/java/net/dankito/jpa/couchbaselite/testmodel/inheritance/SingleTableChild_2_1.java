package net.dankito.jpa.couchbaselite.testmodel.inheritance;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by ganymed on 24/08/16.
 */
@Entity
@DiscriminatorValue(value = "21")
public class SingleTableChild_2_1 extends SingleTableBase {

  @Column
  protected int zipCode; // yeah i know, a zipCode gets modelled as String, just to have more different kind of data types


  public SingleTableChild_2_1() {

  }

  public SingleTableChild_2_1(String name, int zipCode) {
    super(name);
    this.zipCode = zipCode;
  }


  public int getZipCode() {
    return zipCode;
  }

  public void setZipCode(int zipCode) {
    this.zipCode = zipCode;
  }

}

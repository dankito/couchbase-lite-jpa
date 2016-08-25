package net.dankito.jpa.couchbaselite.testmodel.inheritance;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by ganymed on 24/08/16.
 */
@Entity
@DiscriminatorValue(value = "1")
public class SingleTableChild_1 extends SingleTableBase {

  @Column
  protected int age;


  public SingleTableChild_1() {

  }

  public SingleTableChild_1(String name, int age) {
    super(name);
    this.age = age;
  }


  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

}

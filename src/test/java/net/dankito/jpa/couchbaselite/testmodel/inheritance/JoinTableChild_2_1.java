package net.dankito.jpa.couchbaselite.testmodel.inheritance;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by ganymed on 24/08/16.
 */
@Entity
@DiscriminatorValue(value = "Child_2_1")
public class JoinTableChild_2_1 extends JoinTableChild_2_MappedSuperclass {

  public static final String HEIGHT_COLUMN_NAME = "height";


  @Column(name = HEIGHT_COLUMN_NAME)
  protected double heightInMeter;


  public JoinTableChild_2_1() {

  }

  public JoinTableChild_2_1(String name, String givenName, double heightInMeter) {
    super(name, givenName);
    this.heightInMeter = heightInMeter;
  }


  public double getHeightInMeter() {
    return heightInMeter;
  }

  public void setHeightInMeter(double heightInMeter) {
    this.heightInMeter = heightInMeter;
  }

}

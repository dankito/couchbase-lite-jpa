package net.dankito.jpa.couchbaselite.testmodel.inheritance;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Created by ganymed on 24/08/16.
 */
@Entity
@DiscriminatorValue(value = "Child_1")
public class JoinTableChild_1 extends JoinedTableBase {

  public static final String DAY_OF_BIRTH_COLUMN_NAME = "day_of_birth";


  @Column(name = DAY_OF_BIRTH_COLUMN_NAME)
  @Temporal(value = TemporalType.DATE)
  protected Date dayOfBirth;


  public JoinTableChild_1() {

  }

  public JoinTableChild_1(String name, Date dayOfBirth) {
    super(name);
    this.dayOfBirth = dayOfBirth;
  }


  public Date getDayOfBirth() {
    return dayOfBirth;
  }

  public void setDayOfBirth(Date dayOfBirth) {
    this.dayOfBirth = dayOfBirth;
  }

}

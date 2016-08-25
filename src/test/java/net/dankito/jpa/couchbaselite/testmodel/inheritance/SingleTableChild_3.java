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
@DiscriminatorValue(value = "3")
public class SingleTableChild_3 extends SingleTableBase {

  @Column
  @Temporal(value = TemporalType.TIMESTAMP) // yeah, i know i should be DATE, but just for having more variants
  protected Date dayOfBirth;


  public SingleTableChild_3() {

  }

  public SingleTableChild_3(String name, Date dayOfBirth) {
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

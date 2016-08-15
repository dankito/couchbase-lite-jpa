package net.dankito.jpa.couchbaselite.testmodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Created by ganymed on 15/08/16.
 */
@Entity
public class EntityWithAllDataTypes extends BaseEntity {

  public static final String NAME_COLUMN_NAME = "name";

  public static final String AGE_COLUMN_NAME = "age";

  public static final String DAY_OF_BIRTH_COLUMN_NAME = "day_of_birth";

  public static final String IS_MARRIED_COLUMN_NAME = "is_married";


  @Column(name = NAME_COLUMN_NAME)
  protected String name;

  @Column(name = AGE_COLUMN_NAME)
  protected int age;

  @Column(name = DAY_OF_BIRTH_COLUMN_NAME)
  protected Date dayOfBirth;

  @Column(name = IS_MARRIED_COLUMN_NAME)
  protected boolean isMarried;


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public Date getDayOfBirth() {
    return dayOfBirth;
  }

  public void setDayOfBirth(Date dayOfBirth) {
    this.dayOfBirth = dayOfBirth;
  }

  public boolean isMarried() {
    return isMarried;
  }

  public void setMarried(boolean married) {
    isMarried = married;
  }
}

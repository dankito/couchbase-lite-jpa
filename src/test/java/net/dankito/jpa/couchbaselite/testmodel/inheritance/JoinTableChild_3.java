package net.dankito.jpa.couchbaselite.testmodel.inheritance;

import net.dankito.jpa.couchbaselite.testmodel.enums.Gender;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by ganymed on 24/08/16.
 */
@Entity
@DiscriminatorValue(value = "Child_3")
public class JoinTableChild_3 extends JoinedTableBase {

  public static final String GENDER_COLUMN_NAME = "gender";


  @Column(name = GENDER_COLUMN_NAME)
  @Enumerated(value = EnumType.ORDINAL)
  protected Gender gender;


  public JoinTableChild_3() {

  }

  public JoinTableChild_3(String name, Gender gender) {
    super(name);
    setGender(gender);
  }


  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

}

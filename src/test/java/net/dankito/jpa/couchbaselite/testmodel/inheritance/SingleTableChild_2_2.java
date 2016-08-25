package net.dankito.jpa.couchbaselite.testmodel.inheritance;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by ganymed on 24/08/16.
 */
@Entity
@DiscriminatorValue(value = "22")
public class SingleTableChild_2_2 extends SingleTableBase {

  @Column
  protected boolean hasMoreThanOneMillionResidents;


  public SingleTableChild_2_2() {

  }

  public SingleTableChild_2_2(String name, boolean hasMoreThanOneMillionResidents) {
    super(name);
    this.hasMoreThanOneMillionResidents = hasMoreThanOneMillionResidents;
  }


  public boolean isHasMoreThanOneMillionResidents() {
    return hasMoreThanOneMillionResidents;
  }

  public void setHasMoreThanOneMillionResidents(boolean hasMoreThanOneMillionResidents) {
    this.hasMoreThanOneMillionResidents = hasMoreThanOneMillionResidents;
  }

}

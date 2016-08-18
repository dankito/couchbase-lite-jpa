package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Created by ganymed on 18/08/16.
 */
@Entity
public class OneToOneOwningEntity extends BaseEntity {

  public static final String INVERSE_SIDE_COLUMN_NAME = "inverse_side";


  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = INVERSE_SIDE_COLUMN_NAME)
  protected OneToOneInverseEntity inverseSide;


  public OneToOneOwningEntity() {

  }

  public OneToOneOwningEntity(OneToOneInverseEntity inverseSide) {
    this.inverseSide = inverseSide;

    if(inverseSide != null) {
      inverseSide.setOwningSide(this);
    }
  }


  public OneToOneInverseEntity getInverseSide() {
    return inverseSide;
  }

}

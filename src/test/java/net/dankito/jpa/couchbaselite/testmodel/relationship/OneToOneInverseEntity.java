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
public class OneToOneInverseEntity extends BaseEntity {


  @OneToOne(mappedBy = "inverseSide", cascade = CascadeType.ALL)
  protected OneToOneOwningEntity owningSide;


  public OneToOneOwningEntity getOwningSide() {
    return owningSide;
  }

  public void setOwningSide(OneToOneOwningEntity owningSide) {
    this.owningSide = owningSide;
  }
}

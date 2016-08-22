package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Created by ganymed on 18/08/16.
 */
@Entity
public class OneToManyUnidirectionalInverseEntity extends BaseEntity {

  @Column
  protected int order = -1;


  public OneToManyUnidirectionalInverseEntity() {

  }

  public OneToManyUnidirectionalInverseEntity(int order) {
    this.order = order;
  }


  public int getOrder() {
    return order;
  }

}

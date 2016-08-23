package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

/**
 * Created by ganymed on 18/08/16.
 */
@MappedSuperclass
public abstract class OneToManyBidirectionalManySideEntity extends BaseEntity {

  public static final String ONE_SIDE_COLUMN_NAME = "one_side";

  @Column
  protected int order = -1;


  public OneToManyBidirectionalManySideEntity() {

  }

  public OneToManyBidirectionalManySideEntity(int order) {
    this.order = order;
  }

  public abstract OneToManyBidirectionalOneSideEntity getOneSide();

  public abstract void setOneSide(OneToManyBidirectionalOneSideEntity oneSide);


  public int getOrder() {
    return order;
  }

}

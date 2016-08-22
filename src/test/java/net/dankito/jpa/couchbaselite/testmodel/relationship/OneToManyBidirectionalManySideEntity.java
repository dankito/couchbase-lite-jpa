package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Created by ganymed on 18/08/16.
 */
@Entity
public class OneToManyBidirectionalManySideEntity extends BaseEntity {

  public static final String ONE_SIDE_COLUMN_NAME = "one_side";


  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = ONE_SIDE_COLUMN_NAME)
  protected OneToManyBidirectionalOneSideEntity oneSide;

  @Column
  protected int order = -1;


  public OneToManyBidirectionalManySideEntity() {

  }

  public OneToManyBidirectionalManySideEntity(int order) {
    this.order = order;
  }

  public OneToManyBidirectionalOneSideEntity getOneSide() {
    return oneSide;
  }

  public void setOneSide(OneToManyBidirectionalOneSideEntity oneSide) {
    this.oneSide = oneSide;
  }


  public int getOrder() {
    return order;
  }

}

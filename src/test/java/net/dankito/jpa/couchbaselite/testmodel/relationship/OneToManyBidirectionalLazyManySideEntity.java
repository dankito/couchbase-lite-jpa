package net.dankito.jpa.couchbaselite.testmodel.relationship;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Created by ganymed on 18/08/16.
 */
@Entity
public class OneToManyBidirectionalLazyManySideEntity extends OneToManyBidirectionalManySideEntity {

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = ONE_SIDE_COLUMN_NAME)
  protected OneToManyBidirectionalLazyOneSideEntity oneSide;


  public OneToManyBidirectionalLazyManySideEntity() {

  }

  public OneToManyBidirectionalLazyManySideEntity(int order) {
    super(order);
  }


  public OneToManyBidirectionalOneSideEntity getOneSide() {
    return oneSide;
  }

  public void setOneSide(OneToManyBidirectionalOneSideEntity oneSide) {
    this.oneSide = (OneToManyBidirectionalLazyOneSideEntity)oneSide;
  }

}

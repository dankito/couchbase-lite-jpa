package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;

/**
 * Created by ganymed on 18/08/16.
 */
@MappedSuperclass
public abstract class ManyToManyBidirectionalInverseSideEntity extends BaseEntity {

  @Column
  protected int order = -1;


  public ManyToManyBidirectionalInverseSideEntity() {

  }

  public ManyToManyBidirectionalInverseSideEntity(int order) {
    this.order = order;
  }


  public abstract Collection<ManyToManyBidirectionalOwningSideEntity> getOwningSides();

  protected abstract void addOwningSide(ManyToManyBidirectionalOwningSideEntity owningSide);

  protected abstract void removeOwningSide(ManyToManyBidirectionalOwningSideEntity owningSide);


  public int getOrder() {
    return order;
  }

}

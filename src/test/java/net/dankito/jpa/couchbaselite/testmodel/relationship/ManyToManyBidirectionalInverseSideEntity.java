package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

/**
 * Created by ganymed on 18/08/16.
 */
@Entity
public class ManyToManyBidirectionalInverseSideEntity extends BaseEntity {


  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "inverseSides")
  protected Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = new HashSet<>();

  @Column
  protected int order = -1;


  public ManyToManyBidirectionalInverseSideEntity() {

  }

  public ManyToManyBidirectionalInverseSideEntity(int order) {
    this.order = order;
  }


  public Collection<ManyToManyBidirectionalOwningSideEntity> getOwningSides() {
    return owningSides;
  }

  protected void addOwningSide(ManyToManyBidirectionalOwningSideEntity owningSide) {
    this.owningSides.add(owningSide);
  }

  protected void removeOwningSide(ManyToManyBidirectionalOwningSideEntity owningSide) {
    this.owningSides.remove(owningSide);
  }


  public int getOrder() {
    return order;
  }

}

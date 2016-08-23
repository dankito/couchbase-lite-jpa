package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

/**
 * Created by ganymed on 18/08/16.
 */
@Entity
public class ManyToManyBidirectionalEagerInverseSideEntity extends ManyToManyBidirectionalInverseSideEntity {


  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "inverseSides")
  protected Collection<ManyToManyBidirectionalEagerOwningSideEntity> owningSides = new HashSet<>();


  public ManyToManyBidirectionalEagerInverseSideEntity() {

  }

  public ManyToManyBidirectionalEagerInverseSideEntity(int order) {
    super(order);
  }


  public Collection<ManyToManyBidirectionalOwningSideEntity> getOwningSides() {
    List<ManyToManyBidirectionalOwningSideEntity> castedOwningSides = new ArrayList<>();

    for(ManyToManyBidirectionalEagerOwningSideEntity owningSide : owningSides) {
      castedOwningSides.add(owningSide);
    }

    return castedOwningSides;
  }

  protected void addOwningSide(ManyToManyBidirectionalOwningSideEntity owningSide) {
    this.owningSides.add((ManyToManyBidirectionalEagerOwningSideEntity)owningSide);
  }

  protected void removeOwningSide(ManyToManyBidirectionalOwningSideEntity owningSide) {
    this.owningSides.remove(owningSide);
  }


}

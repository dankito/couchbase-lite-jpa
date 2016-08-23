package net.dankito.jpa.couchbaselite.testmodel.relationship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;

/**
 * Created by ganymed on 18/08/16.
 */
@Entity
public class ManyToManyBidirectionalLazyInverseSideEntity extends ManyToManyBidirectionalInverseSideEntity {


  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "inverseSides")
  protected Collection<ManyToManyBidirectionalLazyOwningSideEntity> owningSides = new HashSet<>();


  public ManyToManyBidirectionalLazyInverseSideEntity() {

  }

  public ManyToManyBidirectionalLazyInverseSideEntity(int order) {
    super(order);
  }


  public Collection<ManyToManyBidirectionalOwningSideEntity> getOwningSides() {
    List<ManyToManyBidirectionalOwningSideEntity> castedOwningSides = new ArrayList<>();

    for(ManyToManyBidirectionalLazyOwningSideEntity owningSide : owningSides) {
      castedOwningSides.add(owningSide);
    }

    return castedOwningSides;
  }

  protected void addOwningSide(ManyToManyBidirectionalOwningSideEntity owningSide) {
    this.owningSides.add((ManyToManyBidirectionalLazyOwningSideEntity)owningSide);
  }

  protected void removeOwningSide(ManyToManyBidirectionalOwningSideEntity owningSide) {
    this.owningSides.remove(owningSide);
  }


}

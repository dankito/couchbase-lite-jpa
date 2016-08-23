package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 * Created by ganymed on 18/08/16.
 */
@Entity
public class OneToManyBidirectionalEagerOneSideEntity extends OneToManyBidirectionalOneSideEntity {


  @OneToMany(mappedBy = "oneSide", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @OrderBy("order ASC")
  protected Collection<OneToManyBidirectionalEagerManySideEntity> manySides = null;


  public OneToManyBidirectionalEagerOneSideEntity() {

  }

  public OneToManyBidirectionalEagerOneSideEntity(Collection<OneToManyBidirectionalEagerManySideEntity> manySides) {
    if(manySides != null) {
      for (OneToManyBidirectionalEagerManySideEntity item : manySides) {
        addManySide(item);
      }
    }
  }


  @Override
  public Collection<OneToManyBidirectionalManySideEntity> getManySides() {
    List<OneToManyBidirectionalManySideEntity> castedManySides = new ArrayList<>();

    for(OneToManyBidirectionalEagerManySideEntity manySide : manySides) {
      castedManySides.add(manySide);
    }

    return castedManySides;
  }

  @Override
  public void addManySide(OneToManyBidirectionalManySideEntity manySide) {
    if(manySides == null) {
      manySides = new HashSet<>();
    }

    if(manySide != null) {
      manySide.setOneSide(this);

      manySides.add((OneToManyBidirectionalEagerManySideEntity)manySide);
    }
  }

  @Override
  public void removeManySide(OneToManyBidirectionalManySideEntity manySide) {
    if(manySide != null) {
      manySide.setOneSide(null);

      manySides.remove((OneToManyBidirectionalEagerManySideEntity)manySide);
    }
  }
}

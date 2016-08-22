package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 * Created by ganymed on 18/08/16.
 */
@Entity
public class OneToManyBidirectionalOneSideEntity extends BaseEntity {


  @OneToMany(mappedBy = "oneSide", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @OrderBy("order ASC")
  protected Collection<OneToManyBidirectionalManySideEntity> manySides = new HashSet<>();


  public OneToManyBidirectionalOneSideEntity() {

  }

  public OneToManyBidirectionalOneSideEntity(Collection<OneToManyBidirectionalManySideEntity> manySides) {
    if(this.manySides != null) {
      for(OneToManyBidirectionalManySideEntity manySide : this.manySides) {
        manySide.setOneSide(null);
      }
    }

    this.manySides = manySides;

    if(manySides != null) {
      for (OneToManyBidirectionalManySideEntity item : manySides) {
        item.setOneSide(this);
      }
    }
  }


  public Collection<OneToManyBidirectionalManySideEntity> getManySides() {
    return manySides;
  }

}

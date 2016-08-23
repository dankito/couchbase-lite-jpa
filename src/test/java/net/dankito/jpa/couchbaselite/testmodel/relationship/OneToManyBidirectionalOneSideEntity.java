package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 * Created by ganymed on 18/08/16.
 */
@MappedSuperclass
public abstract class OneToManyBidirectionalOneSideEntity extends BaseEntity {


  public OneToManyBidirectionalOneSideEntity() {

  }


  public abstract Collection<OneToManyBidirectionalManySideEntity> getManySides();

  public abstract void addManySide(OneToManyBidirectionalManySideEntity manySide);

  public abstract void removeManySide(OneToManyBidirectionalManySideEntity manySide);

}

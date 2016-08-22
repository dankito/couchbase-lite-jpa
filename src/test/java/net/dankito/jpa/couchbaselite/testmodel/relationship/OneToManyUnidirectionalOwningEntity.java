package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 * Created by ganymed on 18/08/16.
 */
@Entity
public class OneToManyUnidirectionalOwningEntity extends BaseEntity {


  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @OrderBy("order ASC")
  protected Collection<OneToManyUnidirectionalInverseEntity> inverseSides = new HashSet<>();


  public OneToManyUnidirectionalOwningEntity() {

  }

  public OneToManyUnidirectionalOwningEntity(Collection<OneToManyUnidirectionalInverseEntity> inverseSides) {
    this.inverseSides = inverseSides;
  }


  public Collection<OneToManyUnidirectionalInverseEntity> getInverseSides() {
    return inverseSides;
  }

}

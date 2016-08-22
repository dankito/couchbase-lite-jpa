package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 18/08/16.
 */
@Entity
public class OneToManyUnidirectionalOwningEntity extends BaseEntity {

  public static final String INVERSE_SIDES_COLUMN_NAME = "inverse_sides";


  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = INVERSE_SIDES_COLUMN_NAME)
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

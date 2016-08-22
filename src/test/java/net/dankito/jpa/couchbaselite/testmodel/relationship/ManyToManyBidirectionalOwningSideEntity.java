package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;

/**
 * Created by ganymed on 18/08/16.
 */
@Entity
public class ManyToManyBidirectionalOwningSideEntity extends BaseEntity {

  public static final String JOIN_TABLE_NAME = "owning_side_inverse_side_join_table";

  public static final String JOIN_TABLE_OWNING_SIDE_COLUMN_NAME = "owning_side_id";
  public static final String JOIN_TABLE_INVERSE_SIDE_COLUMN_NAME = "inverse_side_id";


  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
      name = JOIN_TABLE_NAME,
      joinColumns = { @JoinColumn(name = JOIN_TABLE_OWNING_SIDE_COLUMN_NAME) },
      inverseJoinColumns = { @JoinColumn(name = JOIN_TABLE_INVERSE_SIDE_COLUMN_NAME) }
  )
  @OrderBy("order ASC")
  protected Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides;


  public ManyToManyBidirectionalOwningSideEntity() {

  }

  public ManyToManyBidirectionalOwningSideEntity(Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides) {
    if(this.inverseSides != null) {
      for(ManyToManyBidirectionalInverseSideEntity manySide : this.inverseSides) {
        manySide.removeOwningSide(null);
      }
    }

    this.inverseSides = inverseSides;

    if(inverseSides != null) {
      for (ManyToManyBidirectionalInverseSideEntity item : inverseSides) {
        item.addOwningSide(this);
      }
    }
  }


  public Collection<ManyToManyBidirectionalInverseSideEntity> getInverseSides() {
    return inverseSides;
  }

}

package net.dankito.jpa.couchbaselite.testmodel.relationship;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.OrderBy;

/**
 * Created by ganymed on 18/08/16.
 */
@MappedSuperclass
public abstract class ManyToManyBidirectionalOwningSideEntity extends BaseEntity {

  public static final String JOIN_TABLE_NAME = "owning_side_inverse_side_join_table";

  public static final String JOIN_TABLE_OWNING_SIDE_COLUMN_NAME = "owning_side_id";
  public static final String JOIN_TABLE_INVERSE_SIDE_COLUMN_NAME = "inverse_side_id";


  public ManyToManyBidirectionalOwningSideEntity() {

  }


  public abstract Collection<ManyToManyBidirectionalInverseSideEntity> getInverseSides();

  public abstract void addInverseSide(ManyToManyBidirectionalInverseSideEntity inverseSide);

  public abstract void removeInverseSide(ManyToManyBidirectionalInverseSideEntity inverseSide);

}

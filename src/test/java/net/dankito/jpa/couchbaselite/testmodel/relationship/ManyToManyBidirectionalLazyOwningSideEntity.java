package net.dankito.jpa.couchbaselite.testmodel.relationship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;

/**
 * Created by ganymed on 18/08/16.
 */
@Entity
public class ManyToManyBidirectionalLazyOwningSideEntity extends ManyToManyBidirectionalOwningSideEntity {

  public static final String JOIN_TABLE_NAME = "owning_side_inverse_side_join_table";

  public static final String JOIN_TABLE_OWNING_SIDE_COLUMN_NAME = "owning_side_id";
  public static final String JOIN_TABLE_INVERSE_SIDE_COLUMN_NAME = "inverse_side_id";


  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
      name = JOIN_TABLE_NAME,
      joinColumns = { @JoinColumn(name = JOIN_TABLE_OWNING_SIDE_COLUMN_NAME) },
      inverseJoinColumns = { @JoinColumn(name = JOIN_TABLE_INVERSE_SIDE_COLUMN_NAME) }
  )
  @OrderBy("order ASC")
  protected Collection<ManyToManyBidirectionalLazyInverseSideEntity> inverseSides;


  public ManyToManyBidirectionalLazyOwningSideEntity() {

  }

  public ManyToManyBidirectionalLazyOwningSideEntity(Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides) {
    if(inverseSides != null) {
      for (ManyToManyBidirectionalInverseSideEntity item : inverseSides) {
        addInverseSide(item);
      }
    }
  }


  public Collection<ManyToManyBidirectionalInverseSideEntity> getInverseSides() {
    List<ManyToManyBidirectionalInverseSideEntity> castedInverseSides = new ArrayList<>();

    for(ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) {
      castedInverseSides.add(inverseSide);
    }

    return castedInverseSides;
  }

  public void addInverseSide(ManyToManyBidirectionalInverseSideEntity inverseSide) {
    if(inverseSide != null) {
      inverseSide.addOwningSide(this);

      if(inverseSides == null) {
        inverseSides = new HashSet<>();
      }

      inverseSides.add((ManyToManyBidirectionalLazyInverseSideEntity)inverseSide);
    }
  }

  public void removeInverseSide(ManyToManyBidirectionalInverseSideEntity inverseSide) {
    if(inverseSide != null) {
      inverseSide.removeOwningSide(this);

      inverseSides.remove(inverseSide);
    }
  }

}

package net.dankito.jpa.couchbaselite.relationship;

import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalEagerManySideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalEagerOneSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalManySideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalOneSideEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 18/08/16.
 */
public class OneToManyBidirectionalEagerRelationshipDaoTest extends OneToManyBidirectionalRelationshipDaoTestBase {


  @Override
  protected Class[] getEntitiesToTest() {
    return new Class[] { OneToManyBidirectionalEagerOneSideEntity.class, OneToManyBidirectionalEagerManySideEntity.class };
  }


  protected OneToManyBidirectionalOneSideEntity createTestOneSideEntity(Collection<OneToManyBidirectionalManySideEntity> manySides) {
    List<OneToManyBidirectionalEagerManySideEntity> castedManySides = new ArrayList<>();

    if(manySides != null) {
      for (OneToManyBidirectionalManySideEntity manySide : manySides) {
        castedManySides.add((OneToManyBidirectionalEagerManySideEntity) manySide);
      }
    }

    return new OneToManyBidirectionalEagerOneSideEntity(castedManySides);
  }

  protected OneToManyBidirectionalManySideEntity createTestManySideEntity(int order) {
    return new OneToManyBidirectionalEagerManySideEntity(order);
  }

  protected Dao getManySideDao() {
    return daoCache.getDaoForEntity(OneToManyBidirectionalEagerManySideEntity.class);
  }

}

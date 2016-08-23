package net.dankito.jpa.couchbaselite.relationship;

import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalEagerManySideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalEagerOneSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalLazyManySideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalLazyOneSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalManySideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalOneSideEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 18/08/16.
 */
public class OneToManyBidirectionalLazyRelationshipDaoTest extends OneToManyBidirectionalRelationshipDaoTestBase {


  @Override
  protected Class[] getEntitiesToTest() {
    return new Class[] { OneToManyBidirectionalLazyOneSideEntity.class, OneToManyBidirectionalLazyManySideEntity.class };
  }


  protected OneToManyBidirectionalOneSideEntity createTestOneSideEntity(Collection<OneToManyBidirectionalManySideEntity> manySides) {
    List<OneToManyBidirectionalLazyManySideEntity> castedManySides = new ArrayList<>();

    if(manySides != null) {
      for (OneToManyBidirectionalManySideEntity manySide : manySides) {
        castedManySides.add((OneToManyBidirectionalLazyManySideEntity) manySide);
      }
    }

    return new OneToManyBidirectionalLazyOneSideEntity(castedManySides);
  }

  protected OneToManyBidirectionalManySideEntity createTestManySideEntity(int order) {
    return new OneToManyBidirectionalLazyManySideEntity(order);
  }

  protected Dao getManySideDao() {
    return relationshipDaoCache.getDaoForEntity(OneToManyBidirectionalLazyManySideEntity.class);
  }

}

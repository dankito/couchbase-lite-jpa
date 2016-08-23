package net.dankito.jpa.couchbaselite.relationship;

import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalEagerInverseSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalEagerOwningSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalInverseSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalOwningSideEntity;

import java.util.List;

/**
 * Created by ganymed on 23/08/16.
 */
public class ManyToManyBidirectionalEagerRelationshipDaoTest extends ManyToManyBidirectionalRelationshipDaoTest {


  @Override
  protected Class[] getEntitiesToTest() {
    return new Class[] { ManyToManyBidirectionalEagerOwningSideEntity.class, ManyToManyBidirectionalEagerInverseSideEntity.class };
  }


  protected ManyToManyBidirectionalOwningSideEntity createTestOwningSideEntity(List<ManyToManyBidirectionalInverseSideEntity> inverseSides) {
    return new ManyToManyBidirectionalEagerOwningSideEntity(inverseSides);
  }

  protected ManyToManyBidirectionalInverseSideEntity createTestInverseSideEntity(int order) {
    return new ManyToManyBidirectionalEagerInverseSideEntity(order);
  }

  protected Dao getTargetDao() {
    return relationshipDaoCache.getDaoForEntity(ManyToManyBidirectionalEagerInverseSideEntity.class);
  }

}

package net.dankito.jpa.couchbaselite.relationship;

import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalInverseSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalLazyInverseSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalLazyOwningSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalOwningSideEntity;

import java.util.List;

/**
 * Created by ganymed on 23/08/16.
 */
public class ManyToManyBidirectionalLazyRelationshipDaoTest extends ManyToManyBidirectionalRelationshipDaoTestBase {


  @Override
  protected Class[] getEntitiesToTest() {
    return new Class[] { ManyToManyBidirectionalLazyOwningSideEntity.class, ManyToManyBidirectionalLazyInverseSideEntity.class };
  }


  protected ManyToManyBidirectionalOwningSideEntity createTestOwningSideEntity(List<ManyToManyBidirectionalInverseSideEntity> inverseSides) {
    return new ManyToManyBidirectionalLazyOwningSideEntity(inverseSides);
  }

  protected ManyToManyBidirectionalInverseSideEntity createTestInverseSideEntity(int order) {
    return new ManyToManyBidirectionalLazyInverseSideEntity(order);
  }

  protected Dao getTargetDao() {
    return relationshipDaoCache.getDaoForEntity(ManyToManyBidirectionalLazyInverseSideEntity.class);
  }

}

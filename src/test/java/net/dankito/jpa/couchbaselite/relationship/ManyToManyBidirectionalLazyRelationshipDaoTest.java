package net.dankito.jpa.couchbaselite.relationship;

import com.couchbase.lite.CouchbaseLiteException;

import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalInverseSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalLazyInverseSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalLazyOwningSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalOwningSideEntity;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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


  @Test
  public void checkIfEntitiesReallyGetLazilyLoaded() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = createTestOwningSideEntities(inverseSides);

    for(ManyToManyBidirectionalOwningSideEntity owningSide : owningSides) {
      objectCache.clear();

      ManyToManyBidirectionalLazyOwningSideEntity persistedOwningSide = (ManyToManyBidirectionalLazyOwningSideEntity) underTest.retrieve(owningSide.getId());

      // getInverseSides().size() is greater 0, but there hasn't been and ManyToManyBidirectionalLazyInverseSideEntity created yet
      Assert.assertEquals(COUNT_INVERSE_SIDES_PER_OWNING_SIDE, persistedOwningSide.getUncastedInverseSides().size());
      Assert.assertEquals(0, objectCache.getAllOfClass(ManyToManyBidirectionalLazyInverseSideEntity.class).size());

      // now resolve all inverse sides
      List<ManyToManyBidirectionalLazyInverseSideEntity> resolvedInverseSides = new ArrayList<>(persistedOwningSide.getUncastedInverseSides());

      Assert.assertEquals(persistedOwningSide.getUncastedInverseSides().size(), objectCache.getAllOfClass(ManyToManyBidirectionalLazyInverseSideEntity.class).size());
    }
  }

}

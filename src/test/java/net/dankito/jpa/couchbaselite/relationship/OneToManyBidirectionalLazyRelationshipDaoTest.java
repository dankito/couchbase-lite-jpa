package net.dankito.jpa.couchbaselite.relationship;

import com.couchbase.lite.CouchbaseLiteException;

import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalLazyManySideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalLazyOneSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalManySideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalOneSideEntity;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
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


  @Test
  public void checkIfEntitiesReallyGetLazilyLoaded() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyBidirectionalManySideEntity> manySides = createTestManySideEntities();
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(manySides);

    underTest.create(oneSide);

    objectCache.clear();

    OneToManyBidirectionalLazyOneSideEntity persistedOneSide = (OneToManyBidirectionalLazyOneSideEntity) underTest.retrieve(oneSide.getId());

    // getInverseSides().size() is greater 0, but there hasn't been and OneToManyBidirectionalLazyManySideEntity created yet
    Assert.assertEquals(COUNT_TEST_MANY_SIDE_ENTITIES, persistedOneSide.getUncastedManySides().size());
    Assert.assertEquals(0, objectCache.getAllOfClass(OneToManyBidirectionalLazyManySideEntity.class).size());

    // now resolve all inverse sides
    List<OneToManyBidirectionalLazyManySideEntity> resolvedInverseSides = new ArrayList<>(persistedOneSide.getUncastedManySides());

    Assert.assertEquals(persistedOneSide.getUncastedManySides().size(), objectCache.getAllOfClass(OneToManyBidirectionalLazyManySideEntity.class).size());
  }

}

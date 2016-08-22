package net.dankito.jpa.couchbaselite.relationship;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.jpa.couchbaselite.DaoTestBase;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyUnidirectionalInverseEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyUnidirectionalOwningEntity;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ganymed on 18/08/16.
 */
public class OneToManyUnidirectionalRelationshipDaoTest extends DaoTestBase {

  public static final int COUNT_TEST_INVERSE_SIDE_ENTITIES = 4;


  @Override
  protected Class[] getEntitiesToTest() {
    return new Class[] { OneToManyUnidirectionalOwningEntity.class, OneToManyUnidirectionalInverseEntity.class };
  }


  @Test
  public void oneToManyCreate_AllPropertiesGetPersistedCorrectly() throws Exception {
    Collection<OneToManyUnidirectionalInverseEntity> inverseSides = createTestInverseSideEntities();
    OneToManyUnidirectionalOwningEntity owningSide = new OneToManyUnidirectionalOwningEntity(inverseSides);

    underTest.create(owningSide);

    Document persistedOwningSideDocument = database.getDocument(owningSide.getId());
    Assert.assertNotNull(persistedOwningSideDocument);

    String itemIdsString = (String)persistedOwningSideDocument.getProperty("inverseSides");
    ObjectMapper objectMapper = new ObjectMapper();
    List itemIds = objectMapper.readValue(itemIdsString, List.class);

    Assert.assertEquals(itemIds.size(), owningSide.getInverseSides().size());

    for(OneToManyUnidirectionalInverseEntity inverseSide : inverseSides) {
      Assert.assertTrue(itemIds.contains(inverseSide.getId()));

      Document persistedInverseSideDocument = database.getDocument(inverseSide.getId());
      Assert.assertNotNull(persistedInverseSideDocument);
    }
  }

  @Test
  public void oneToManyCreate_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyUnidirectionalInverseEntity> inverseSides = createTestInverseSideEntities();
    OneToManyUnidirectionalOwningEntity owningSide = new OneToManyUnidirectionalOwningEntity(inverseSides);

    underTest.create(owningSide);

    Assert.assertNotNull(owningSide.getId());
    Assert.assertNotNull(owningSide.getVersion());
    Assert.assertNotNull(owningSide.getCreatedOn());
    Assert.assertNotNull(owningSide.getModifiedOn());
    Assert.assertEquals(owningSide.getCreatedOn(), owningSide.getModifiedOn());

    for(OneToManyUnidirectionalInverseEntity inverseSide : inverseSides) {
      Assert.assertNotNull(inverseSide.getId());
      Assert.assertNotNull(inverseSide.getVersion());
      Assert.assertNotNull(inverseSide.getCreatedOn());
      Assert.assertNotNull(inverseSide.getModifiedOn());
      Assert.assertEquals(inverseSide.getCreatedOn(), inverseSide.getModifiedOn());
    }
  }

  @Test
  public void oneToManyCreate_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyUnidirectionalInverseEntity> inverseSides = createTestInverseSideEntities();
    OneToManyUnidirectionalOwningEntity owningSide = new OneToManyUnidirectionalOwningEntity(inverseSides);

    underTest.create(owningSide);

    Assert.assertTrue(owningSide.hasPrePersistBeenCalled());
    Assert.assertTrue(owningSide.hasPostPersistBeenCalled());
    Assert.assertFalse(owningSide.hasPostLoadBeenCalled());
    Assert.assertFalse(owningSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(owningSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(owningSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(owningSide.hasPostRemoveBeenCalled());

    for(OneToManyUnidirectionalInverseEntity inverseSide : inverseSides) {
      Assert.assertTrue(inverseSide.hasPrePersistBeenCalled());
      Assert.assertTrue(inverseSide.hasPostPersistBeenCalled());
      Assert.assertFalse(inverseSide.hasPostLoadBeenCalled());
      Assert.assertFalse(inverseSide.hasPreUpdateBeenCalled());
      Assert.assertFalse(inverseSide.hasPostUpdateBeenCalled());
      Assert.assertFalse(inverseSide.hasPreRemoveBeenCalled());
      Assert.assertFalse(inverseSide.hasPostRemoveBeenCalled());
    }
  }


  @Test
  public void oneToManyCreate_InverseSideIsNull_NoExceptionsAndAllPropertiesGetPersistedCorrectly() throws CouchbaseLiteException, SQLException {
    OneToManyUnidirectionalOwningEntity owningSide = new OneToManyUnidirectionalOwningEntity(null);

    underTest.create(owningSide);

    Document persistedOwningSideDocument = database.getDocument(owningSide.getId());
    Assert.assertNotNull(persistedOwningSideDocument);

    Assert.assertEquals(null, persistedOwningSideDocument.getProperty(OneToManyUnidirectionalOwningEntity.INVERSE_SIDES_COLUMN_NAME + "_id"));
  }


  @Test
  public void oneToManyRetrieve_AllPropertiesAreSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyUnidirectionalInverseEntity> inverseSides = createTestInverseSideEntities();
    OneToManyUnidirectionalOwningEntity owningSide = new OneToManyUnidirectionalOwningEntity(inverseSides);

    underTest.create(owningSide);

    objectCache.clear();

    OneToManyUnidirectionalOwningEntity persistedOwningSide = (OneToManyUnidirectionalOwningEntity) underTest.retrieve(owningSide.getId());
    Assert.assertEquals(COUNT_TEST_INVERSE_SIDE_ENTITIES, persistedOwningSide.getInverseSides().size());

    List<OneToManyUnidirectionalInverseEntity> persistedInverseSides = new ArrayList<>(persistedOwningSide.getInverseSides());

    for(int i = 0; i < COUNT_TEST_INVERSE_SIDE_ENTITIES; i++) {
      OneToManyUnidirectionalInverseEntity persistedInverseSide = persistedInverseSides.get(i);
      Assert.assertEquals(i, persistedInverseSide.getOrder());
    }
  }

  @Test
  public void oneToManyRetrieve_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyUnidirectionalInverseEntity> inverseSides = createTestInverseSideEntities();
    OneToManyUnidirectionalOwningEntity owningSide = new OneToManyUnidirectionalOwningEntity(inverseSides);

    underTest.create(owningSide);

    objectCache.clear();

    OneToManyUnidirectionalOwningEntity persistedOwningSide = (OneToManyUnidirectionalOwningEntity) underTest.retrieve(owningSide.getId());

    Assert.assertNotNull(persistedOwningSide.getId());
    Assert.assertNotNull(persistedOwningSide.getVersion());
    Assert.assertTrue(persistedOwningSide.getVersion().startsWith("2"));
    Assert.assertNotNull(persistedOwningSide.getCreatedOn());
    Assert.assertNotNull(persistedOwningSide.getModifiedOn());

    for(OneToManyUnidirectionalInverseEntity persistedInverseSide : persistedOwningSide.getInverseSides()) {
      Assert.assertNotNull(persistedInverseSide.getId());
      Assert.assertNotNull(persistedInverseSide.getVersion());
      Assert.assertTrue(persistedInverseSide.getVersion().startsWith("1"));
      Assert.assertNotNull(persistedInverseSide.getCreatedOn());
      Assert.assertNotNull(persistedInverseSide.getModifiedOn());
    }
  }

  @Test
  public void oneToManyRetrieve_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyUnidirectionalInverseEntity> inverseSides = createTestInverseSideEntities();
    OneToManyUnidirectionalOwningEntity owningSide = new OneToManyUnidirectionalOwningEntity(inverseSides);

    underTest.create(owningSide);

    objectCache.clear();

    OneToManyUnidirectionalOwningEntity persistedOwningSide = (OneToManyUnidirectionalOwningEntity) underTest.retrieve(owningSide.getId());

    Assert.assertFalse(persistedOwningSide.hasPrePersistBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPostPersistBeenCalled());
    Assert.assertTrue(persistedOwningSide.hasPostLoadBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPostRemoveBeenCalled());

    for(OneToManyUnidirectionalInverseEntity persistedInverseSide : persistedOwningSide.getInverseSides()) {
      Assert.assertFalse(persistedInverseSide.hasPrePersistBeenCalled());
      Assert.assertFalse(persistedInverseSide.hasPostPersistBeenCalled());
      Assert.assertTrue(persistedInverseSide.hasPostLoadBeenCalled());
      Assert.assertFalse(persistedInverseSide.hasPreUpdateBeenCalled());
      Assert.assertFalse(persistedInverseSide.hasPostUpdateBeenCalled());
      Assert.assertFalse(persistedInverseSide.hasPreRemoveBeenCalled());
      Assert.assertFalse(persistedInverseSide.hasPostRemoveBeenCalled());
    }
  }


  protected Collection<OneToManyUnidirectionalInverseEntity> createTestInverseSideEntities() {
    Set<OneToManyUnidirectionalInverseEntity> inverseSides = new HashSet<>();

    for(int i = 0; i < COUNT_TEST_INVERSE_SIDE_ENTITIES; i++) {
      OneToManyUnidirectionalInverseEntity testEntity = new OneToManyUnidirectionalInverseEntity(i);
      inverseSides.add(testEntity);
    }

    return inverseSides;
  }

}

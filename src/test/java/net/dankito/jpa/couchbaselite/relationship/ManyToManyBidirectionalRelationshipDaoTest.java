package net.dankito.jpa.couchbaselite.relationship;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.couchbaselite.DaoTestBase;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalInverseSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalOwningSideEntity;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by ganymed on 18/08/16.
 */
public class ManyToManyBidirectionalRelationshipDaoTest extends DaoTestBase {

  public static final int COUNT_TEST_MANY_SIDE_ENTITIES = 4;

  protected ObjectMapper objectMapper = new ObjectMapper();


  @Override
  protected Class[] getEntitiesToTest() {
    return new Class[] { ManyToManyBidirectionalOwningSideEntity.class, ManyToManyBidirectionalInverseSideEntity.class };
  }


  @Test
  public void manyToManyCreate_AllPropertiesGetPersistedCorrectly() throws Exception {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    ManyToManyBidirectionalOwningSideEntity owningSide = new ManyToManyBidirectionalOwningSideEntity(inverseSides);

    underTest.create(owningSide);

    Dao targetDao = relationshipDaoCache.getDaoForEntity(ManyToManyBidirectionalInverseSideEntity.class);
    for(ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) {
      targetDao.update(inverseSide);
    }

    Document persistedOwningSideDocument = database.getDocument(owningSide.getId());
    Assert.assertNotNull(persistedOwningSideDocument);

    List itemIds = getTargetEntityIds(persistedOwningSideDocument, "owning_side_id"); // TODO: this column name is wrong

    Assert.assertEquals(itemIds.size(), owningSide.getInverseSides().size());

    for(ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) {
      Assert.assertTrue(itemIds.contains(inverseSide.getId()));

      Document persistedInverseSideDocument = database.getDocument(inverseSide.getId());
      Assert.assertNotNull(persistedInverseSideDocument);

      List inverseSideTargetEntityIds = getTargetEntityIds(persistedInverseSideDocument, "owningSides"); // TODO: this column name may be wrong
      Assert.assertTrue(inverseSideTargetEntityIds.contains(owningSide.getId()));
    }
  }

  @Test
  public void manyToManyCreate_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    ManyToManyBidirectionalOwningSideEntity owningSide = new ManyToManyBidirectionalOwningSideEntity(inverseSides);

    underTest.create(owningSide);

    Assert.assertNotNull(owningSide.getId());
    Assert.assertNotNull(owningSide.getVersion());
    Assert.assertNotNull(owningSide.getCreatedOn());
    Assert.assertNotNull(owningSide.getModifiedOn());
    Assert.assertEquals(owningSide.getCreatedOn(), owningSide.getModifiedOn());

    for(ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) {
      Assert.assertNotNull(inverseSide.getId());
      Assert.assertNotNull(inverseSide.getVersion());
      Assert.assertNotNull(inverseSide.getCreatedOn());
      Assert.assertNotNull(inverseSide.getModifiedOn());
      Assert.assertEquals(inverseSide.getCreatedOn(), inverseSide.getModifiedOn());
    }
  }

  @Test
  public void manyToManyCreate_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    ManyToManyBidirectionalOwningSideEntity owningSide = new ManyToManyBidirectionalOwningSideEntity(inverseSides);

    underTest.create(owningSide);

    Assert.assertTrue(owningSide.hasPrePersistBeenCalled());
    Assert.assertTrue(owningSide.hasPostPersistBeenCalled());
    Assert.assertFalse(owningSide.hasPostLoadBeenCalled());
    Assert.assertFalse(owningSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(owningSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(owningSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(owningSide.hasPostRemoveBeenCalled());

    for(ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) {
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
  public void manyToManyCreate_InverseSideIsNull_NoExceptionsAndAllPropertiesGetPersistedCorrectly() throws CouchbaseLiteException, SQLException {
    ManyToManyBidirectionalOwningSideEntity owningSide = new ManyToManyBidirectionalOwningSideEntity(null);

    underTest.create(owningSide);

    Document persistedOneSideDocument = database.getDocument(owningSide.getId());
    Assert.assertNotNull(persistedOneSideDocument);

    Assert.assertEquals(null, persistedOneSideDocument.getProperty("owning_side_id"));
  }


  @Test
  public void manyToManyRetrieve_AllPropertiesAreSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    ManyToManyBidirectionalOwningSideEntity owningSide = new ManyToManyBidirectionalOwningSideEntity(inverseSides);

    underTest.create(owningSide);

    objectCache.clear();

    ManyToManyBidirectionalOwningSideEntity persistedOwningSide = (ManyToManyBidirectionalOwningSideEntity) underTest.retrieve(owningSide.getId());
    Assert.assertEquals(COUNT_TEST_MANY_SIDE_ENTITIES, persistedOwningSide.getInverseSides().size());

    List<ManyToManyBidirectionalInverseSideEntity> persistedInverseSides = new ArrayList<>(persistedOwningSide.getInverseSides());

    for(int i = 0; i < COUNT_TEST_MANY_SIDE_ENTITIES; i++) {
      ManyToManyBidirectionalInverseSideEntity persistedInverseSide = persistedInverseSides.get(i);
      Assert.assertEquals(i, persistedInverseSide.getOrder());
    }
  }

  @Test
  public void manyToManyRetrieve_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    ManyToManyBidirectionalOwningSideEntity owningSide = new ManyToManyBidirectionalOwningSideEntity(inverseSides);

    underTest.create(owningSide);

    objectCache.clear();

    ManyToManyBidirectionalOwningSideEntity persistedOwningSide = (ManyToManyBidirectionalOwningSideEntity) underTest.retrieve(owningSide.getId());

    Assert.assertNotNull(persistedOwningSide.getId());
    Assert.assertNotNull(persistedOwningSide.getVersion());
    Assert.assertTrue(persistedOwningSide.getVersion().startsWith("1"));
    Assert.assertNotNull(persistedOwningSide.getCreatedOn());
    Assert.assertNotNull(persistedOwningSide.getModifiedOn());

    for(ManyToManyBidirectionalInverseSideEntity persistedInverseSide : persistedOwningSide.getInverseSides()) {
      Assert.assertNotNull(persistedInverseSide.getId());
      Assert.assertNotNull(persistedInverseSide.getVersion());
      Assert.assertTrue(persistedInverseSide.getVersion().startsWith("1"));
      Assert.assertNotNull(persistedInverseSide.getCreatedOn());
      Assert.assertNotNull(persistedInverseSide.getModifiedOn());
    }
  }

  @Test
  public void manyToManyRetrieve_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    ManyToManyBidirectionalOwningSideEntity owningSide = new ManyToManyBidirectionalOwningSideEntity(inverseSides);

    underTest.create(owningSide);

    objectCache.clear();

    ManyToManyBidirectionalOwningSideEntity persistedOwningSide = (ManyToManyBidirectionalOwningSideEntity) underTest.retrieve(owningSide.getId());

    Assert.assertFalse(persistedOwningSide.hasPrePersistBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPostPersistBeenCalled());
    Assert.assertTrue(persistedOwningSide.hasPostLoadBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPostRemoveBeenCalled());

    for(ManyToManyBidirectionalInverseSideEntity persistedInverseSide : persistedOwningSide.getInverseSides()) {
      Assert.assertFalse(persistedInverseSide.hasPrePersistBeenCalled());
      Assert.assertFalse(persistedInverseSide.hasPostPersistBeenCalled());
      Assert.assertTrue(persistedInverseSide.hasPostLoadBeenCalled());
      Assert.assertFalse(persistedInverseSide.hasPreUpdateBeenCalled());
      Assert.assertFalse(persistedInverseSide.hasPostUpdateBeenCalled());
      Assert.assertFalse(persistedInverseSide.hasPreRemoveBeenCalled());
      Assert.assertFalse(persistedInverseSide.hasPostRemoveBeenCalled());
    }
  }


  @Test
  public void manyToManyUpdate_EntityGetsUpdatedCorrectly() throws CouchbaseLiteException, SQLException, IOException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    ManyToManyBidirectionalOwningSideEntity owningSide = new ManyToManyBidirectionalOwningSideEntity(inverseSides);

    underTest.create(owningSide);

    Dao targetDao = relationshipDaoCache.getDaoForEntity(ManyToManyBidirectionalInverseSideEntity.class);
    for(ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) {
      targetDao.update(inverseSide);
    }

    updateEntity_Delete2Add3InverseSides(owningSide);

    underTest.update(owningSide);

    Document persistedOwningSideDocument = database.getDocument(owningSide.getId());
    Assert.assertNotNull(persistedOwningSideDocument);

    List itemIds = getTargetEntityIds(persistedOwningSideDocument, "owning_side_id");
    Assert.assertEquals(itemIds.size(), owningSide.getInverseSides().size());

    objectCache.clear();

    ManyToManyBidirectionalOwningSideEntity persistedOwningSide = (ManyToManyBidirectionalOwningSideEntity) underTest.retrieve(owningSide.getId());
    Assert.assertEquals(COUNT_TEST_MANY_SIDE_ENTITIES + 1, persistedOwningSide.getInverseSides().size());

    List<ManyToManyBidirectionalInverseSideEntity> persistedInverseSides = new ArrayList<>(persistedOwningSide.getInverseSides());

    for(int i = 0; i < COUNT_TEST_MANY_SIDE_ENTITIES + 1; i++) {
      ManyToManyBidirectionalInverseSideEntity persistedInverseSide = persistedInverseSides.get(i);
      Assert.assertEquals(i + 2, persistedInverseSide.getOrder());

      Document persistedInverseSideDocument = database.getDocument(persistedInverseSide.getId());
      Assert.assertNotNull(persistedInverseSideDocument);

      List inverseSideTargetEntityIds = getTargetEntityIds(persistedInverseSideDocument, "inverse_side_id");
      Assert.assertTrue(inverseSideTargetEntityIds.contains(owningSide.getId()));
    }
  }

  @Test
  public void manyToManyUpdate_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    ManyToManyBidirectionalOwningSideEntity owningSide = new ManyToManyBidirectionalOwningSideEntity(inverseSides);

    underTest.create(owningSide);

    Date modifiedOnBeforeUpdate = owningSide.getModifiedOn();

    updateEntity_Delete2Add3InverseSides(owningSide);

    underTest.update(owningSide);

    Assert.assertNotNull(owningSide.getId());
    Assert.assertNotNull(owningSide.getVersion());
    Assert.assertTrue(owningSide.getVersion().startsWith("2"));
    Assert.assertNotNull(owningSide.getCreatedOn());
    Assert.assertNotEquals(owningSide.getCreatedOn(), owningSide.getModifiedOn());
    Assert.assertNotNull(owningSide.getModifiedOn());
    Assert.assertNotEquals(modifiedOnBeforeUpdate, owningSide.getModifiedOn());

    for(ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) { // assert inverseSides haven't been updated
      Assert.assertNotNull(inverseSide.getId());
      Assert.assertNotNull(inverseSide.getVersion());
      Assert.assertTrue(inverseSide.getVersion().startsWith("1"));
      Assert.assertNotNull(inverseSide.getCreatedOn());
      Assert.assertEquals(inverseSide.getCreatedOn(), inverseSide.getModifiedOn());
      Assert.assertNotNull(inverseSide.getModifiedOn());
    }
  }

  @Test
  public void manyToManyUpdate_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    ManyToManyBidirectionalOwningSideEntity owningSide = new ManyToManyBidirectionalOwningSideEntity(inverseSides);

    underTest.create(owningSide);

    updateEntity_Delete2Add3InverseSides(owningSide);

    underTest.update(owningSide);

    Assert.assertTrue(owningSide.hasPrePersistBeenCalled());
    Assert.assertTrue(owningSide.hasPostPersistBeenCalled());
    Assert.assertFalse(owningSide.hasPostLoadBeenCalled());
    Assert.assertTrue(owningSide.hasPreUpdateBeenCalled());
    Assert.assertTrue(owningSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(owningSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(owningSide.hasPostRemoveBeenCalled());

    for(ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) { // assert inverseSides haven't been updated
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
  public void manyToManyDelete_EntityGetsDeletedCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    ManyToManyBidirectionalOwningSideEntity owningSide = new ManyToManyBidirectionalOwningSideEntity(inverseSides);

    underTest.create(owningSide);

    Document persistedDocumentBefore = database.getExistingDocument(owningSide.getId());
    Assert.assertNotNull(persistedDocumentBefore);

    underTest.delete(owningSide);

    Document persistedOwningSideDocument = database.getExistingDocument(owningSide.getId());
    Assert.assertNull(persistedOwningSideDocument); // null means it doesn't exist

    for(ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) {
      Document persistedInverseSideDocument = database.getExistingDocument(inverseSide.getId());
      Assert.assertNull(persistedInverseSideDocument); // null means it doesn't exist
    }
  }

  @Test
  public void manyToManyDelete_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    ManyToManyBidirectionalOwningSideEntity owningSide = new ManyToManyBidirectionalOwningSideEntity(inverseSides);

    underTest.create(owningSide);

    Date owningSideModifiedOnBeforeDeletion = owningSide.getModifiedOn();

    Document persistedDocumentBefore = database.getExistingDocument(owningSide.getId());
    Assert.assertNotNull(persistedDocumentBefore);

    underTest.delete(owningSide);

    Assert.assertNotNull(owningSide.getId());
    Assert.assertNull(owningSide.getVersion());
    Assert.assertNotNull(owningSide.getCreatedOn());
    Assert.assertNotEquals(owningSide.getCreatedOn(), owningSide.getModifiedOn());
    Assert.assertNotNull(owningSide.getModifiedOn());
    Assert.assertNotEquals(owningSideModifiedOnBeforeDeletion, owningSide.getModifiedOn());

    // test CascadeType.Remove
    for(ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) {
      Assert.assertNotNull(inverseSide.getId());
      Assert.assertNull(inverseSide.getVersion());
      Assert.assertNotNull(inverseSide.getCreatedOn());
      Assert.assertNotEquals(inverseSide.getCreatedOn(), inverseSide.getModifiedOn());
      Assert.assertNotNull(inverseSide.getModifiedOn());
    }
  }

  @Test
  public void manyToManyDelete_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    ManyToManyBidirectionalOwningSideEntity owningSide = new ManyToManyBidirectionalOwningSideEntity(inverseSides);

    underTest.create(owningSide);

    Document persistedDocumentBefore = database.getExistingDocument(owningSide.getId());
    Assert.assertNotNull(persistedDocumentBefore);

    underTest.delete(owningSide);

    Assert.assertTrue(owningSide.hasPrePersistBeenCalled());
    Assert.assertTrue(owningSide.hasPostPersistBeenCalled());
    Assert.assertFalse(owningSide.hasPostLoadBeenCalled());
    Assert.assertFalse(owningSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(owningSide.hasPostUpdateBeenCalled());
    Assert.assertTrue(owningSide.hasPreRemoveBeenCalled());
    Assert.assertTrue(owningSide.hasPostRemoveBeenCalled());

    // test CascadeType.Remove
    for(ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) {
      Assert.assertTrue(inverseSide.hasPrePersistBeenCalled());
      Assert.assertTrue(inverseSide.hasPostPersistBeenCalled());
      Assert.assertFalse(inverseSide.hasPostLoadBeenCalled());
      Assert.assertFalse(inverseSide.hasPreUpdateBeenCalled());
      Assert.assertFalse(inverseSide.hasPostUpdateBeenCalled());
      Assert.assertTrue(inverseSide.hasPreRemoveBeenCalled());
      Assert.assertTrue(inverseSide.hasPostRemoveBeenCalled());
    }
  }


  protected List getTargetEntityIds(Document document, String propertyName) throws IOException {
    String itemIdsString = (String)document.getProperty(propertyName);
    return objectMapper.readValue(itemIdsString, List.class);
  }


  protected Collection<ManyToManyBidirectionalInverseSideEntity> createTestInverseSideEntities() throws CouchbaseLiteException, SQLException {
    Set<ManyToManyBidirectionalInverseSideEntity> inverseSides = new HashSet<>();
    Dao inverseSideDao = relationshipDaoCache.getDaoForEntity(ManyToManyBidirectionalInverseSideEntity.class);

    for(int i = 0; i < COUNT_TEST_MANY_SIDE_ENTITIES; i++) {
      ManyToManyBidirectionalInverseSideEntity testEntity = new ManyToManyBidirectionalInverseSideEntity(i);
      inverseSideDao.create(testEntity);
      
      inverseSides.add(testEntity);
    }

    return inverseSides;
  }

  protected void updateEntity_Delete2Add3InverseSides(ManyToManyBidirectionalOwningSideEntity owningSide) throws CouchbaseLiteException, SQLException {
    List<ManyToManyBidirectionalInverseSideEntity> itemsToRemove = new ArrayList<>();
    Iterator iterator = owningSide.getInverseSides().iterator();

    while(iterator.hasNext()) {
      ManyToManyBidirectionalInverseSideEntity item = (ManyToManyBidirectionalInverseSideEntity)iterator.next();
      if(item.getOrder() < 2) {
        itemsToRemove.add(item);
      }
    }

    for(ManyToManyBidirectionalInverseSideEntity itemToRemove : itemsToRemove) {
      owningSide.removeInverseSide(itemToRemove);
    }


    Dao inverseSideDao = relationshipDaoCache.getDaoForEntity(ManyToManyBidirectionalInverseSideEntity.class);

    for(int i = COUNT_TEST_MANY_SIDE_ENTITIES; i < COUNT_TEST_MANY_SIDE_ENTITIES + 3; i++) {
      ManyToManyBidirectionalInverseSideEntity testEntity = new ManyToManyBidirectionalInverseSideEntity(i);
      inverseSideDao.create(testEntity);

      owningSide.addInverseSide(testEntity);

      inverseSideDao.update(testEntity);
    }
  }

}

package net.dankito.jpa.couchbaselite.relationship;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.couchbaselite.DaoTestBase;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalEagerManySideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalEagerOneSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalManySideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalOneSideEntity;

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
public abstract class OneToManyBidirectionalRelationshipDaoTestBase extends DaoTestBase {

  public static final int COUNT_TEST_MANY_SIDE_ENTITIES = 4;

  protected ObjectMapper objectMapper = new ObjectMapper();


  protected abstract OneToManyBidirectionalOneSideEntity createTestOneSideEntity(Collection<OneToManyBidirectionalManySideEntity> manySides);

  protected abstract OneToManyBidirectionalManySideEntity createTestManySideEntity(int order);

  protected abstract Dao getManySideDao();


  @Test
  public void oneToManyCreate_AllPropertiesGetPersistedCorrectly() throws Exception {
    Collection<OneToManyBidirectionalManySideEntity> manySides = createTestManySideEntities();
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(manySides);

    underTest.create(oneSide);

    Document persistedOwningSideDocument = database.getDocument(oneSide.getId());
    Assert.assertNotNull(persistedOwningSideDocument);

    List itemIds = getTargetEntityIds(persistedOwningSideDocument, "manySides");

    Assert.assertEquals(itemIds.size(), oneSide.getManySides().size());

    for(OneToManyBidirectionalManySideEntity manySide : manySides) {
      Assert.assertTrue(itemIds.contains(manySide.getId()));

      Document persistedInverseSideDocument = database.getDocument(manySide.getId());
      Assert.assertNotNull(persistedInverseSideDocument);

      Assert.assertEquals(oneSide.getId(), persistedInverseSideDocument.getProperty(OneToManyBidirectionalManySideEntity.ONE_SIDE_COLUMN_NAME));
    }
  }

  @Test
  public void oneToManyCreate_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyBidirectionalManySideEntity> manySides = createTestManySideEntities();
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(manySides);

    underTest.create(oneSide);

    Assert.assertNotNull(oneSide.getId());
    Assert.assertNotNull(oneSide.getVersion());
    Assert.assertNotNull(oneSide.getCreatedOn());
    Assert.assertNotNull(oneSide.getModifiedOn());
    Assert.assertEquals(oneSide.getCreatedOn(), oneSide.getModifiedOn());

    for(OneToManyBidirectionalManySideEntity manySide : manySides) {
      Assert.assertNotNull(manySide.getId());
      Assert.assertNotNull(manySide.getVersion());
      Assert.assertNotNull(manySide.getCreatedOn());
      Assert.assertNotNull(manySide.getModifiedOn());
      Assert.assertEquals(manySide.getCreatedOn(), manySide.getModifiedOn());
    }
  }

  @Test
  public void oneToManyCreate_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyBidirectionalManySideEntity> manySides = createTestManySideEntities();
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(manySides);

    underTest.create(oneSide);

    Assert.assertTrue(oneSide.hasPrePersistBeenCalled());
    Assert.assertTrue(oneSide.hasPostPersistBeenCalled());
    Assert.assertFalse(oneSide.hasPostLoadBeenCalled());
    Assert.assertFalse(oneSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(oneSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(oneSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(oneSide.hasPostRemoveBeenCalled());

    for(OneToManyBidirectionalManySideEntity manySide : manySides) {
      Assert.assertTrue(manySide.hasPrePersistBeenCalled());
      Assert.assertTrue(manySide.hasPostPersistBeenCalled());
      Assert.assertFalse(manySide.hasPostLoadBeenCalled());
      Assert.assertFalse(manySide.hasPreUpdateBeenCalled());
      Assert.assertFalse(manySide.hasPostUpdateBeenCalled());
      Assert.assertFalse(manySide.hasPreRemoveBeenCalled());
      Assert.assertFalse(manySide.hasPostRemoveBeenCalled());
    }
  }


  @Test
  public void oneToManyCreate_InverseSideIsNull_NoExceptionsAndAllPropertiesGetPersistedCorrectly() throws CouchbaseLiteException, SQLException {
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(null);

    underTest.create(oneSide);

    Document persistedOneSideDocument = database.getDocument(oneSide.getId());
    Assert.assertNotNull(persistedOneSideDocument);

    Assert.assertEquals(null, persistedOneSideDocument.getProperty("manySides"));
  }


  @Test
  public void oneToManyRetrieve_AllPropertiesAreSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyBidirectionalManySideEntity> manySides = createTestManySideEntities();
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(manySides);

    underTest.create(oneSide);

    objectCache.clear();

    OneToManyBidirectionalOneSideEntity persistedOwningSide = (OneToManyBidirectionalOneSideEntity) underTest.retrieve(oneSide.getId());
    Assert.assertEquals(COUNT_TEST_MANY_SIDE_ENTITIES, persistedOwningSide.getManySides().size());

    List<OneToManyBidirectionalManySideEntity> persistedInverseSides = new ArrayList<>(persistedOwningSide.getManySides());

    for(int i = 0; i < COUNT_TEST_MANY_SIDE_ENTITIES; i++) {
      OneToManyBidirectionalManySideEntity persistedInverseSide = persistedInverseSides.get(i);
      Assert.assertEquals(i, persistedInverseSide.getOrder());
    }
  }

  @Test
  public void oneToManyRetrieve_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyBidirectionalManySideEntity> manySides = createTestManySideEntities();
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(manySides);

    underTest.create(oneSide);

    objectCache.clear();

    OneToManyBidirectionalOneSideEntity persistedOwningSide = (OneToManyBidirectionalOneSideEntity) underTest.retrieve(oneSide.getId());

    Assert.assertNotNull(persistedOwningSide.getId());
    Assert.assertNotNull(persistedOwningSide.getVersion());
    Assert.assertEquals(2L, (long)persistedOwningSide.getVersion());
    Assert.assertNotNull(persistedOwningSide.getCreatedOn());
    Assert.assertNotNull(persistedOwningSide.getModifiedOn());

    for(OneToManyBidirectionalManySideEntity persistedInverseSide : persistedOwningSide.getManySides()) {
      Assert.assertNotNull(persistedInverseSide.getId());
      Assert.assertNotNull(persistedInverseSide.getVersion());
//      Assert.assertEquals(1L, (long)persistedInverseSide.getVersion()); // can be 1 or 2
      Assert.assertNotNull(persistedInverseSide.getCreatedOn());
      Assert.assertNotNull(persistedInverseSide.getModifiedOn());
    }
  }

  @Test
  public void oneToManyRetrieve_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyBidirectionalManySideEntity> manySides = createTestManySideEntities();
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(manySides);

    underTest.create(oneSide);

    objectCache.clear();

    OneToManyBidirectionalOneSideEntity persistedOwningSide = (OneToManyBidirectionalOneSideEntity) underTest.retrieve(oneSide.getId());

    Assert.assertFalse(persistedOwningSide.hasPrePersistBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPostPersistBeenCalled());
    Assert.assertTrue(persistedOwningSide.hasPostLoadBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPostRemoveBeenCalled());

    for(OneToManyBidirectionalManySideEntity persistedInverseSide : persistedOwningSide.getManySides()) {
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
  public void oneToManyUpdate_EntityGetsUpdatedCorrectly() throws CouchbaseLiteException, SQLException, IOException {
    Collection<OneToManyBidirectionalManySideEntity> manySides = createTestManySideEntities();
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(manySides);

    underTest.create(oneSide);

    updateEntity_Delete2Add3InverseSides(oneSide);

    underTest.update(oneSide);

    Document persistedOwningSideDocument = database.getDocument(oneSide.getId());
    Assert.assertNotNull(persistedOwningSideDocument);

    List itemIds = getTargetEntityIds(persistedOwningSideDocument, "manySides");
    Assert.assertEquals(itemIds.size(), oneSide.getManySides().size());

    objectCache.clear();

    OneToManyBidirectionalOneSideEntity persistedOwningSide = (OneToManyBidirectionalOneSideEntity) underTest.retrieve(oneSide.getId());
    Assert.assertEquals(COUNT_TEST_MANY_SIDE_ENTITIES + 1, persistedOwningSide.getManySides().size());

    List<OneToManyBidirectionalManySideEntity> persistedInverseSides = new ArrayList<>(persistedOwningSide.getManySides());

    for(int i = 0; i < COUNT_TEST_MANY_SIDE_ENTITIES + 1; i++) {
      OneToManyBidirectionalManySideEntity persistedInverseSide = persistedInverseSides.get(i);
      Assert.assertEquals(i + 2, persistedInverseSide.getOrder());

      Document persistedInverseSideDocument = database.getDocument(persistedInverseSide.getId());
      Assert.assertNotNull(persistedInverseSideDocument);

      Assert.assertEquals(oneSide.getId(), persistedInverseSideDocument.getProperty(OneToManyBidirectionalManySideEntity.ONE_SIDE_COLUMN_NAME));
    }
  }

  @Test
  public void oneToManyUpdate_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyBidirectionalManySideEntity> manySides = createTestManySideEntities();
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(manySides);

    underTest.create(oneSide);

    Date modifiedOnBeforeUpdate = oneSide.getModifiedOn();

    updateEntity_Delete2Add3InverseSides(oneSide);

    underTest.update(oneSide);

    Assert.assertNotNull(oneSide.getId());
    Assert.assertNotNull(oneSide.getVersion());
    Assert.assertEquals(3L, (long)oneSide.getVersion());
    Assert.assertNotNull(oneSide.getCreatedOn());
    Assert.assertNotEquals(oneSide.getCreatedOn(), oneSide.getModifiedOn());
    Assert.assertNotNull(oneSide.getModifiedOn());
    Assert.assertNotEquals(modifiedOnBeforeUpdate, oneSide.getModifiedOn());

    for(OneToManyBidirectionalManySideEntity inverseSide : manySides) { // assert manySides haven't been updated
      Assert.assertNotNull(inverseSide.getId());
      Assert.assertNotNull(inverseSide.getVersion());
      Assert.assertEquals(1L, (long)inverseSide.getVersion());
      Assert.assertNotNull(inverseSide.getCreatedOn());
      Assert.assertEquals(inverseSide.getCreatedOn(), inverseSide.getModifiedOn());
      Assert.assertNotNull(inverseSide.getModifiedOn());
    }
  }

  @Test
  public void oneToManyUpdate_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyBidirectionalManySideEntity> manySides = createTestManySideEntities();
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(manySides);

    underTest.create(oneSide);

    updateEntity_Delete2Add3InverseSides(oneSide);

    underTest.update(oneSide);

    Assert.assertTrue(oneSide.hasPrePersistBeenCalled());
    Assert.assertTrue(oneSide.hasPostPersistBeenCalled());
    Assert.assertFalse(oneSide.hasPostLoadBeenCalled());
    Assert.assertTrue(oneSide.hasPreUpdateBeenCalled());
    Assert.assertTrue(oneSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(oneSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(oneSide.hasPostRemoveBeenCalled());

    for(OneToManyBidirectionalManySideEntity inverseSide : manySides) { // assert manySides haven't been updated
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
  public void oneToManyDelete_EntityGetsDeletedCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyBidirectionalManySideEntity> manySides = createTestManySideEntities();
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(manySides);

    underTest.create(oneSide);

    Document persistedDocumentBefore = database.getExistingDocument(oneSide.getId());
    Assert.assertNotNull(persistedDocumentBefore);

    underTest.delete(oneSide);

    Document persistedOwningSideDocument = database.getExistingDocument(oneSide.getId());
    Assert.assertNull(persistedOwningSideDocument); // null means it doesn't exist

    for(OneToManyBidirectionalManySideEntity inverseSide : manySides) {
      Document document = database.getDocument(inverseSide.getId());
      Assert.assertTrue(document.isDeleted());

      Document persistedInverseSideDocument = database.getExistingDocument(inverseSide.getId());
      Assert.assertNull(persistedInverseSideDocument); // null means it doesn't exist
    }
  }

  @Test
  public void oneToManyDelete_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyBidirectionalManySideEntity> manySides = createTestManySideEntities();
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(manySides);

    underTest.create(oneSide);

    Date owningSideModifiedOnBeforeDeletion = oneSide.getModifiedOn();

    Document persistedDocumentBefore = database.getExistingDocument(oneSide.getId());
    Assert.assertNotNull(persistedDocumentBefore);

    underTest.delete(oneSide);

    Assert.assertNotNull(oneSide.getId());
    Assert.assertNull(oneSide.getVersion());
    Assert.assertNotNull(oneSide.getCreatedOn());
    Assert.assertNotEquals(oneSide.getCreatedOn(), oneSide.getModifiedOn());
    Assert.assertNotNull(oneSide.getModifiedOn());
    Assert.assertNotEquals(owningSideModifiedOnBeforeDeletion, oneSide.getModifiedOn());

    // test CascadeType.Remove
    for(OneToManyBidirectionalManySideEntity inverseSide : manySides) {
      Assert.assertNotNull(inverseSide.getId());
      Assert.assertNull(inverseSide.getVersion());
      Assert.assertNotNull(inverseSide.getCreatedOn());
      Assert.assertNotEquals(inverseSide.getCreatedOn(), inverseSide.getModifiedOn());
      Assert.assertNotNull(inverseSide.getModifiedOn());
    }
  }

  @Test
  public void oneToManyDelete_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<OneToManyBidirectionalManySideEntity> manySides = createTestManySideEntities();
    OneToManyBidirectionalOneSideEntity oneSide = createTestOneSideEntity(manySides);

    underTest.create(oneSide);

    Document persistedDocumentBefore = database.getExistingDocument(oneSide.getId());
    Assert.assertNotNull(persistedDocumentBefore);

    underTest.delete(oneSide);

    Assert.assertTrue(oneSide.hasPrePersistBeenCalled());
    Assert.assertTrue(oneSide.hasPostPersistBeenCalled());
    Assert.assertFalse(oneSide.hasPostLoadBeenCalled());
    Assert.assertFalse(oneSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(oneSide.hasPostUpdateBeenCalled());
    Assert.assertTrue(oneSide.hasPreRemoveBeenCalled());
    Assert.assertTrue(oneSide.hasPostRemoveBeenCalled());

    // test CascadeType.Remove
    for(OneToManyBidirectionalManySideEntity inverseSide : manySides) {
      Assert.assertTrue(inverseSide.hasPrePersistBeenCalled());
      Assert.assertTrue(inverseSide.hasPostPersistBeenCalled());
      Assert.assertFalse(inverseSide.hasPostLoadBeenCalled());
      Assert.assertFalse(inverseSide.hasPreUpdateBeenCalled());
      Assert.assertFalse(inverseSide.hasPostUpdateBeenCalled());
      Assert.assertTrue(inverseSide.hasPreRemoveBeenCalled());
      Assert.assertTrue(inverseSide.hasPostRemoveBeenCalled());
    }
  }


  protected List getTargetEntityIds(Document document, String propertyName) throws java.io.IOException {
    String itemIdsString = (String)document.getProperty(propertyName);
    return objectMapper.readValue(itemIdsString, List.class);
  }


  protected Collection<OneToManyBidirectionalManySideEntity> createTestManySideEntities() {
    Set<OneToManyBidirectionalManySideEntity> manySides = new HashSet<>();

    for(int i = 0; i < COUNT_TEST_MANY_SIDE_ENTITIES; i++) {
      OneToManyBidirectionalManySideEntity testEntity = createTestManySideEntity(i);
      manySides.add(testEntity);
    }

    return manySides;
  }

  protected void updateEntity_Delete2Add3InverseSides(OneToManyBidirectionalOneSideEntity oneSide) throws CouchbaseLiteException, SQLException {
    List<OneToManyBidirectionalManySideEntity> itemsToRemove = new ArrayList<>();
    Iterator iterator = oneSide.getManySides().iterator();

    while(iterator.hasNext()) {
      OneToManyBidirectionalManySideEntity item = (OneToManyBidirectionalManySideEntity)iterator.next();
      if(item.getOrder() < 2) {
        itemsToRemove.add(item);
      }
    }

    for(OneToManyBidirectionalManySideEntity itemToRemove : itemsToRemove) {
      oneSide.removeManySide(itemToRemove);
    }


    Dao manySideDao = getManySideDao();

    for(int i = COUNT_TEST_MANY_SIDE_ENTITIES; i < COUNT_TEST_MANY_SIDE_ENTITIES + 3; i++) {
      OneToManyBidirectionalManySideEntity testEntity = createTestManySideEntity(i);
      manySideDao.create(testEntity);

      oneSide.addManySide(testEntity);

      manySideDao.update(testEntity);
    }
  }

}

package net.dankito.jpa.couchbaselite.relationship;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.couchbaselite.DaoTestBase;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalEagerInverseSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.ManyToManyBidirectionalEagerOwningSideEntity;
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
import java.util.Random;
import java.util.Set;

/**
 * Created by ganymed on 18/08/16.
 */
public abstract class ManyToManyBidirectionalRelationshipDaoTestBase extends DaoTestBase {

  public static final int COUNT_TEST_OWNING_SIDE_ENTITIES = 5;

  public static final int COUNT_TEST_INVERSE_SIDE_ENTITIES = 6;

  public static final int COUNT_INVERSE_SIDES_PER_OWNING_SIDE = 3;

  protected ObjectMapper objectMapper = new ObjectMapper();


  protected abstract ManyToManyBidirectionalOwningSideEntity createTestOwningSideEntity(List<ManyToManyBidirectionalInverseSideEntity> inverseSides);

  protected abstract ManyToManyBidirectionalInverseSideEntity createTestInverseSideEntity(int order);

  protected abstract Dao getTargetDao();


  @Test
  public void manyToManyCreate_AllPropertiesGetPersistedCorrectly() throws Exception {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = createTestOwningSideEntities(inverseSides);

    for(ManyToManyBidirectionalOwningSideEntity owningSide : owningSides) {
      Document persistedOwningSideDocument = database.getDocument(owningSide.getId());
      Assert.assertNotNull(persistedOwningSideDocument);

      List itemIds = getTargetEntityIds(persistedOwningSideDocument, "owning_side_id"); // TODO: this column name is wrong

      Assert.assertEquals(itemIds.size(), owningSide.getInverseSides().size());
      Assert.assertEquals(COUNT_INVERSE_SIDES_PER_OWNING_SIDE, owningSide.getInverseSides().size());

      for(ManyToManyBidirectionalInverseSideEntity inverseSide : owningSide.getInverseSides()) {
        Assert.assertTrue(itemIds.contains(inverseSide.getId()));

        Document persistedInverseSideDocument = database.getDocument(inverseSide.getId());
        Assert.assertNotNull(persistedInverseSideDocument);

        List inverseSideTargetEntityIds = getTargetEntityIds(persistedInverseSideDocument, "owningSides"); // TODO: this column name may be wrong
        Assert.assertTrue(inverseSideTargetEntityIds.contains(owningSide.getId()));
      }
    }
  }

  @Test
  public void manyToManyCreate_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = createTestOwningSideEntities(inverseSides);

    for(ManyToManyBidirectionalOwningSideEntity owningSide : owningSides) {
      Assert.assertNotNull(owningSide.getId());
      Assert.assertNotNull(owningSide.getVersion());
      Assert.assertTrue(owningSide.getVersion().startsWith("1"));
      Assert.assertNotNull(owningSide.getCreatedOn());
      Assert.assertNotNull(owningSide.getModifiedOn());
      Assert.assertEquals(owningSide.getCreatedOn(), owningSide.getModifiedOn());

      for (ManyToManyBidirectionalInverseSideEntity inverseSide : owningSide.getInverseSides()) {
        Assert.assertNotNull(inverseSide.getId());
        Assert.assertNotNull(inverseSide.getVersion());
        Assert.assertTrue(inverseSide.getVersion().startsWith("1"));
        Assert.assertNotNull(inverseSide.getCreatedOn());
        Assert.assertNotNull(inverseSide.getModifiedOn());
        Assert.assertEquals(inverseSide.getCreatedOn(), inverseSide.getModifiedOn());
      }
    }
  }

  @Test
  public void manyToManyCreate_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = createTestOwningSideEntities(inverseSides);

    for(ManyToManyBidirectionalOwningSideEntity owningSide : owningSides) {
      Assert.assertTrue(owningSide.hasPrePersistBeenCalled());
      Assert.assertTrue(owningSide.hasPostPersistBeenCalled());
      Assert.assertFalse(owningSide.hasPostLoadBeenCalled());
      Assert.assertFalse(owningSide.hasPreUpdateBeenCalled());
      Assert.assertFalse(owningSide.hasPostUpdateBeenCalled());
      Assert.assertFalse(owningSide.hasPreRemoveBeenCalled());
      Assert.assertFalse(owningSide.hasPostRemoveBeenCalled());

      for (ManyToManyBidirectionalInverseSideEntity inverseSide : owningSide.getInverseSides()) {
        Assert.assertTrue(inverseSide.hasPrePersistBeenCalled());
        Assert.assertTrue(inverseSide.hasPostPersistBeenCalled());
        Assert.assertFalse(inverseSide.hasPostLoadBeenCalled());
        Assert.assertFalse(inverseSide.hasPreUpdateBeenCalled());
        Assert.assertFalse(inverseSide.hasPostUpdateBeenCalled());
        Assert.assertFalse(inverseSide.hasPreRemoveBeenCalled());
        Assert.assertFalse(inverseSide.hasPostRemoveBeenCalled());
      }
    }
  }


  @Test
  public void manyToManyCreate_InverseSideIsNull_NoExceptionsAndAllPropertiesGetPersistedCorrectly() throws CouchbaseLiteException, SQLException {
    ManyToManyBidirectionalOwningSideEntity owningSide = createTestOwningSideEntity(null);

    underTest.create(owningSide);

    Document persistedOneSideDocument = database.getDocument(owningSide.getId());
    Assert.assertNotNull(persistedOneSideDocument);

    Assert.assertEquals(null, persistedOneSideDocument.getProperty("owning_side_id"));
  }


  @Test
  public void manyToManyRetrieve_AllPropertiesAreSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = createTestOwningSideEntities(inverseSides);

    objectCache.clear();

    for(ManyToManyBidirectionalOwningSideEntity owningSide : owningSides) {
      ManyToManyBidirectionalOwningSideEntity persistedOwningSide = (ManyToManyBidirectionalOwningSideEntity) underTest.retrieve(owningSide.getId());
      Assert.assertEquals(COUNT_INVERSE_SIDES_PER_OWNING_SIDE, persistedOwningSide.getInverseSides().size());

      List<ManyToManyBidirectionalInverseSideEntity> persistedInverseSides = new ArrayList<>(persistedOwningSide.getInverseSides());
      int lastOrderNumber = -1;

      for (int i = 0; i < persistedInverseSides.size(); i++) {
        ManyToManyBidirectionalInverseSideEntity persistedInverseSide = persistedInverseSides.get(i);

        Assert.assertTrue(persistedInverseSide.getOrder() > lastOrderNumber); // Inverse Side have to be sorted ascending after their order number
        lastOrderNumber = persistedInverseSide.getOrder();

        Assert.assertTrue(persistedInverseSide.getOwningSides().contains(persistedOwningSide));
      }
    }
  }

  @Test
  public void manyToManyRetrieve_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = createTestOwningSideEntities(inverseSides);

    objectCache.clear();

    for (ManyToManyBidirectionalOwningSideEntity owningSide : owningSides) {
      ManyToManyBidirectionalOwningSideEntity persistedOwningSide = (ManyToManyBidirectionalOwningSideEntity) underTest.retrieve(owningSide.getId());

      Assert.assertNotNull(persistedOwningSide.getId());
      Assert.assertNotNull(persistedOwningSide.getVersion());
      Assert.assertTrue(persistedOwningSide.getVersion().startsWith("2"));
      Assert.assertNotNull(persistedOwningSide.getCreatedOn());
      Assert.assertNotNull(persistedOwningSide.getModifiedOn());

      for (ManyToManyBidirectionalInverseSideEntity persistedInverseSide : persistedOwningSide.getInverseSides()) {
        Assert.assertNotNull(persistedInverseSide.getId());
        Assert.assertNotNull(persistedInverseSide.getVersion());
        Assert.assertTrue(persistedInverseSide.getVersion().startsWith("2"));
        Assert.assertNotNull(persistedInverseSide.getCreatedOn());
        Assert.assertNotNull(persistedInverseSide.getModifiedOn());
      }
    }
  }

  @Test
  public void manyToManyRetrieve_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = createTestOwningSideEntities(inverseSides);

    objectCache.clear();

    for (ManyToManyBidirectionalOwningSideEntity owningSide : owningSides) {
      ManyToManyBidirectionalOwningSideEntity persistedOwningSide = (ManyToManyBidirectionalOwningSideEntity) underTest.retrieve(owningSide.getId());

      Assert.assertFalse(persistedOwningSide.hasPrePersistBeenCalled());
      Assert.assertFalse(persistedOwningSide.hasPostPersistBeenCalled());
      Assert.assertTrue(persistedOwningSide.hasPostLoadBeenCalled());
      Assert.assertFalse(persistedOwningSide.hasPreUpdateBeenCalled());
      Assert.assertFalse(persistedOwningSide.hasPostUpdateBeenCalled());
      Assert.assertFalse(persistedOwningSide.hasPreRemoveBeenCalled());
      Assert.assertFalse(persistedOwningSide.hasPostRemoveBeenCalled());

      for (ManyToManyBidirectionalInverseSideEntity persistedInverseSide : persistedOwningSide.getInverseSides()) {
        Assert.assertFalse(persistedInverseSide.hasPrePersistBeenCalled());
        Assert.assertFalse(persistedInverseSide.hasPostPersistBeenCalled());
        Assert.assertTrue(persistedInverseSide.hasPostLoadBeenCalled());
        Assert.assertFalse(persistedInverseSide.hasPreUpdateBeenCalled());
        Assert.assertFalse(persistedInverseSide.hasPostUpdateBeenCalled());
        Assert.assertFalse(persistedInverseSide.hasPreRemoveBeenCalled());
        Assert.assertFalse(persistedInverseSide.hasPostRemoveBeenCalled());
      }
    }
  }


  @Test
  public void manyToManyUpdate_EntityGetsUpdatedCorrectly() throws CouchbaseLiteException, SQLException, IOException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = createTestOwningSideEntities(inverseSides);

    for (ManyToManyBidirectionalOwningSideEntity owningSide : owningSides) {
      updateEntity_Delete2Add3InverseSides(owningSide);

      underTest.update(owningSide);

      objectCache.clear();

      Document persistedOwningSideDocument = database.getDocument(owningSide.getId());
      Assert.assertNotNull(persistedOwningSideDocument);

      List itemIds = getTargetEntityIds(persistedOwningSideDocument, "owning_side_id");
      Assert.assertEquals(itemIds.size(), owningSide.getInverseSides().size());

      ManyToManyBidirectionalOwningSideEntity persistedOwningSide = (ManyToManyBidirectionalOwningSideEntity) underTest.retrieve(owningSide.getId());
      Assert.assertEquals(COUNT_INVERSE_SIDES_PER_OWNING_SIDE + 1, persistedOwningSide.getInverseSides().size());

      List<ManyToManyBidirectionalInverseSideEntity> persistedInverseSides = new ArrayList<>(persistedOwningSide.getInverseSides());
      int lastOrderNumber = -1;

      for (ManyToManyBidirectionalInverseSideEntity persistedInverseSide : persistedInverseSides) {
        Assert.assertTrue(persistedInverseSide.getOrder() > lastOrderNumber); // Inverse Side have to be sorted ascending after their order number
        lastOrderNumber = persistedInverseSide.getOrder();

        Document persistedInverseSideDocument = database.getDocument(persistedInverseSide.getId());
        Assert.assertNotNull(persistedInverseSideDocument);

        List inverseSideTargetEntityIds = getTargetEntityIds(persistedInverseSideDocument, "owningSides");
        Assert.assertTrue(inverseSideTargetEntityIds.contains(owningSide.getId()));
      }
    }
  }

  @Test
  public void manyToManyUpdate_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = createTestOwningSideEntities(inverseSides);

    for(ManyToManyBidirectionalOwningSideEntity owningSide : owningSides) {
      Date modifiedOnBeforeUpdate = owningSide.getModifiedOn();

      updateEntity_Delete2Add3InverseSides(owningSide);

      underTest.update(owningSide);

      Assert.assertNotNull(owningSide.getId());
      Assert.assertNotNull(owningSide.getVersion());
      Assert.assertTrue(owningSide.getVersion().startsWith("3"));
      Assert.assertNotNull(owningSide.getCreatedOn());
      Assert.assertNotEquals(owningSide.getCreatedOn(), owningSide.getModifiedOn());
      Assert.assertNotNull(owningSide.getModifiedOn());
      Assert.assertNotEquals(modifiedOnBeforeUpdate, owningSide.getModifiedOn());

      for (ManyToManyBidirectionalInverseSideEntity inverseSide : owningSide.getInverseSides()) {
        Assert.assertNotNull(inverseSide.getId());
        Assert.assertNotNull(inverseSide.getVersion());
        Assert.assertNotNull(inverseSide.getCreatedOn());
        Assert.assertNotNull(inverseSide.getModifiedOn());
      }
    }
  }

  @Test
  public void manyToManyUpdate_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = createTestOwningSideEntities(inverseSides);

    for (ManyToManyBidirectionalOwningSideEntity owningSide : owningSides) {
      updateEntity_Delete2Add3InverseSides(owningSide);

      underTest.update(owningSide);

      Assert.assertTrue(owningSide.hasPrePersistBeenCalled());
      Assert.assertTrue(owningSide.hasPostPersistBeenCalled());
      Assert.assertFalse(owningSide.hasPostLoadBeenCalled());
      Assert.assertTrue(owningSide.hasPreUpdateBeenCalled());
      Assert.assertTrue(owningSide.hasPostUpdateBeenCalled());
      Assert.assertFalse(owningSide.hasPreRemoveBeenCalled());
      Assert.assertFalse(owningSide.hasPostRemoveBeenCalled());

      for (ManyToManyBidirectionalInverseSideEntity inverseSide : owningSide.getInverseSides()) {
        Assert.assertTrue(inverseSide.hasPrePersistBeenCalled());
        Assert.assertTrue(inverseSide.hasPostPersistBeenCalled());
        Assert.assertFalse(inverseSide.hasPostLoadBeenCalled());
        // depending on if inverseSide instance got added to other owningSide or not it got updated or not
//        Assert.assertFalse(inverseSide.hasPreUpdateBeenCalled());
//        Assert.assertFalse(inverseSide.hasPostUpdateBeenCalled());
        Assert.assertFalse(inverseSide.hasPreRemoveBeenCalled());
        Assert.assertFalse(inverseSide.hasPostRemoveBeenCalled());
      }
    }
  }


  @Test
  public void manyToManyDelete_EntityGetsDeletedCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = createTestOwningSideEntities(inverseSides);

    for(ManyToManyBidirectionalOwningSideEntity owningSide : owningSides) {
      Document persistedDocumentBefore = database.getExistingDocument(owningSide.getId());
      Assert.assertNotNull(persistedDocumentBefore);

      underTest.delete(owningSide);

      Document persistedOwningSideDocument = database.getExistingDocument(owningSide.getId());
      Assert.assertNull(persistedOwningSideDocument); // null means it doesn't exist

      for (ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) {
        Document document = database.getDocument(inverseSide.getId());
        Assert.assertTrue(document.isDeleted());

        Document persistedInverseSideDocument = database.getExistingDocument(inverseSide.getId());
        Assert.assertNull(persistedInverseSideDocument); // null means it doesn't exist
      }
    }
  }

  @Test
  public void manyToManyDelete_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = createTestOwningSideEntities(inverseSides);

    for(ManyToManyBidirectionalOwningSideEntity owningSide : owningSides) {
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
      for (ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) {
        Assert.assertNotNull(inverseSide.getId());
        Assert.assertNull(inverseSide.getVersion());
        Assert.assertNotNull(inverseSide.getCreatedOn());
        Assert.assertNotEquals(inverseSide.getCreatedOn(), inverseSide.getModifiedOn());
        Assert.assertNotNull(inverseSide.getModifiedOn());
      }
    }
  }

  @Test
  public void manyToManyDelete_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides = createTestInverseSideEntities();
    Collection<ManyToManyBidirectionalOwningSideEntity> owningSides = createTestOwningSideEntities(inverseSides);

    for(ManyToManyBidirectionalOwningSideEntity owningSide : owningSides) {
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
      for (ManyToManyBidirectionalInverseSideEntity inverseSide : inverseSides) {
        Assert.assertTrue(inverseSide.hasPrePersistBeenCalled());
        Assert.assertTrue(inverseSide.hasPostPersistBeenCalled());
        Assert.assertFalse(inverseSide.hasPostLoadBeenCalled());
        Assert.assertFalse(inverseSide.hasPreUpdateBeenCalled());
        Assert.assertFalse(inverseSide.hasPostUpdateBeenCalled());
        Assert.assertTrue(inverseSide.hasPreRemoveBeenCalled());
        Assert.assertTrue(inverseSide.hasPostRemoveBeenCalled());
      }
    }
  }


  protected List getTargetEntityIds(Document document, String propertyName) throws IOException {
    String itemIdsString = (String)document.getProperty(propertyName);
    return objectMapper.readValue(itemIdsString, List.class);
  }


  protected Collection<ManyToManyBidirectionalInverseSideEntity> createTestInverseSideEntities() throws CouchbaseLiteException, SQLException {
    Set<ManyToManyBidirectionalInverseSideEntity> inverseSides = new HashSet<>();

    for(int i = 0; i < COUNT_TEST_INVERSE_SIDE_ENTITIES; i++) {
      ManyToManyBidirectionalInverseSideEntity testEntity = createTestInverseSideEntity(i);
      
      inverseSides.add(testEntity);
    }

    return inverseSides;
  }

  protected Collection<ManyToManyBidirectionalOwningSideEntity> createTestOwningSideEntities(Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides) throws
      CouchbaseLiteException, SQLException {
    List<ManyToManyBidirectionalOwningSideEntity> testOwningSides = new ArrayList<>();

    for(int i = 0; i < COUNT_TEST_OWNING_SIDE_ENTITIES; i++) {
      List<ManyToManyBidirectionalInverseSideEntity> randomlySelectedInverseSides = randomlySelectInverseSides(inverseSides);

      ManyToManyBidirectionalOwningSideEntity testEntity = createTestOwningSideEntity(randomlySelectedInverseSides);
      testOwningSides.add(testEntity);
    }

    for(ManyToManyBidirectionalOwningSideEntity testEntity : testOwningSides) {
      underTest.create(testEntity);
    }

    return testOwningSides;
  }

  protected List<ManyToManyBidirectionalInverseSideEntity> randomlySelectInverseSides(Collection<ManyToManyBidirectionalInverseSideEntity> inverseSides) {
    List<ManyToManyBidirectionalInverseSideEntity> randomlySelectedInverseSides = new ArrayList<>();
    List<ManyToManyBidirectionalInverseSideEntity> inverseSidesList = new ArrayList<>(inverseSides);
    Random random = new Random(System.currentTimeMillis());

    for(int i = 0; i < COUNT_INVERSE_SIDES_PER_OWNING_SIDE; i++) {
      int nextIndex = random.nextInt(inverseSidesList.size());

      while(true) {
        ManyToManyBidirectionalInverseSideEntity nextItem = inverseSidesList.get(nextIndex);

        if(randomlySelectedInverseSides.contains(nextItem) == false) {
          randomlySelectedInverseSides.add(nextItem);
          break;
        }

        nextIndex++;
        if(nextIndex >= inverseSidesList.size()) {
          nextIndex = 0;
        }
      }
    }

    return randomlySelectedInverseSides;
  }

  protected void updateEntity_Delete2Add3InverseSides(ManyToManyBidirectionalOwningSideEntity owningSide) throws CouchbaseLiteException, SQLException {
    List<ManyToManyBidirectionalInverseSideEntity> currentInverseSidesList = new ArrayList<>(owningSide.getInverseSides());
    for(int i = 0; i < 2; i++) {
      owningSide.removeInverseSide(currentInverseSidesList.get(i));
    }

    Dao inverseSideDao = getTargetDao();

    for(int i = COUNT_TEST_INVERSE_SIDE_ENTITIES; i < COUNT_TEST_INVERSE_SIDE_ENTITIES + 3; i++) {
      ManyToManyBidirectionalInverseSideEntity testEntity = createTestInverseSideEntity(i);
      inverseSideDao.create(testEntity);

      owningSide.addInverseSide(testEntity);

      inverseSideDao.update(testEntity);
    }
  }

}

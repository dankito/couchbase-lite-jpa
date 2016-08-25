package net.dankito.jpa.couchbaselite.relationship;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.couchbaselite.DaoTestBase;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToOneInverseEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToOneOwningEntity;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Date;

/**
 * Created by ganymed on 18/08/16.
 */
public class OneToOneBidirectionalRelationshipDaoTest extends DaoTestBase {


  @Override
  protected Class[] getEntitiesToTest() {
    return new Class[] { OneToOneOwningEntity.class, OneToOneInverseEntity.class };
  }


  @Test
  public void oneToOneCreate_AllPropertiesGetPersistedCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    Document persistedOwningSideDocument = database.getDocument(owningSide.getId());
    Assert.assertNotNull(persistedOwningSideDocument);

    Assert.assertEquals(inverseSide.getId(), persistedOwningSideDocument.getProperty(OneToOneOwningEntity.INVERSE_SIDE_COLUMN_NAME));

    Document persistedInverseSideDocument = database.getDocument(inverseSide.getId());
    Assert.assertNotNull(persistedInverseSideDocument);

    Assert.assertEquals(owningSide.getId(), persistedInverseSideDocument.getProperty(OneToOneInverseEntity.OWNING_SIDE_COLUMN_NAME));
  }

  @Test
  public void oneToOneCreate_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    Assert.assertNotNull(owningSide.getId());
    Assert.assertNotNull(owningSide.getVersion());
    Assert.assertNotNull(owningSide.getCreatedOn());
    Assert.assertNotNull(owningSide.getModifiedOn());
    Assert.assertEquals(owningSide.getCreatedOn(), owningSide.getModifiedOn());

    Assert.assertNotNull(inverseSide.getId());
    Assert.assertNotNull(inverseSide.getVersion());
    Assert.assertNotNull(inverseSide.getCreatedOn());
    Assert.assertNotNull(inverseSide.getModifiedOn());
    Assert.assertEquals(inverseSide.getCreatedOn(), inverseSide.getModifiedOn());
  }

  @Test
  public void oneToOneCreate_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    Assert.assertTrue(owningSide.hasPrePersistBeenCalled());
    Assert.assertTrue(owningSide.hasPostPersistBeenCalled());
    Assert.assertFalse(owningSide.hasPostLoadBeenCalled());
    Assert.assertFalse(owningSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(owningSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(owningSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(owningSide.hasPostRemoveBeenCalled());

    Assert.assertTrue(inverseSide.hasPrePersistBeenCalled());
    Assert.assertTrue(inverseSide.hasPostPersistBeenCalled());
    Assert.assertFalse(inverseSide.hasPostLoadBeenCalled());
    Assert.assertFalse(inverseSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(inverseSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(inverseSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(inverseSide.hasPostRemoveBeenCalled());
  }


  @Test
  public void oneToOneCreate_InverseSideIsNull_NoExceptionsAndAllPropertiesGetPersistedCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(null);

    underTest.create(owningSide);

    Document persistedOwningSideDocument = database.getDocument(owningSide.getId());
    Assert.assertNotNull(persistedOwningSideDocument);

    Assert.assertEquals(null, persistedOwningSideDocument.getProperty(OneToOneOwningEntity.INVERSE_SIDE_COLUMN_NAME + "_id"));
  }


  @Test
  public void oneToOneRetrieve_AllPropertiesAreSetCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    objectCache.clear();

    Dao inverseSideDao = daoCache.getDaoForEntity(OneToOneInverseEntity.class);

    OneToOneOwningEntity persistedOwningSide = (OneToOneOwningEntity) underTest.retrieve(owningSide.getId());
    OneToOneInverseEntity persistedInverseSide = (OneToOneInverseEntity) inverseSideDao.retrieve(inverseSide.getId());

    Assert.assertEquals(persistedInverseSide, persistedOwningSide.getInverseSide());

    Assert.assertEquals(persistedOwningSide, persistedInverseSide.getOwningSide());
  }

  @Test
  public void oneToOneRetrieve_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    objectCache.clear();

    OneToOneOwningEntity persistedOwningSide = (OneToOneOwningEntity) underTest.retrieve(owningSide.getId());

    Assert.assertNotNull(persistedOwningSide.getId());
    Assert.assertNotNull(persistedOwningSide.getVersion());
    Assert.assertEquals(2L, (long)persistedOwningSide.getVersion());
    Assert.assertNotNull(persistedOwningSide.getCreatedOn());
    Assert.assertNotNull(persistedOwningSide.getModifiedOn());

    OneToOneInverseEntity persistedInverseSide = persistedOwningSide.getInverseSide();

    Assert.assertNotNull(persistedInverseSide.getId());
    Assert.assertNotNull(persistedInverseSide.getVersion());
    Assert.assertEquals(1L, (long)persistedInverseSide.getVersion());
    Assert.assertNotNull(persistedInverseSide.getCreatedOn());
    Assert.assertNotNull(persistedInverseSide.getModifiedOn());
  }

  @Test
  public void oneToOneRetrieve_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    objectCache.clear();

    OneToOneOwningEntity persistedOwningSide = (OneToOneOwningEntity) underTest.retrieve(owningSide.getId());

    Assert.assertFalse(persistedOwningSide.hasPrePersistBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPostPersistBeenCalled());
    Assert.assertTrue(persistedOwningSide.hasPostLoadBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(persistedOwningSide.hasPostRemoveBeenCalled());

    OneToOneInverseEntity persistedInverseSide = persistedOwningSide.getInverseSide();

    Assert.assertFalse(persistedInverseSide.hasPrePersistBeenCalled());
    Assert.assertFalse(persistedInverseSide.hasPostPersistBeenCalled());
    Assert.assertTrue(persistedInverseSide.hasPostLoadBeenCalled());
    Assert.assertFalse(persistedInverseSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(persistedInverseSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(persistedInverseSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(persistedInverseSide.hasPostRemoveBeenCalled());
  }


  @Test
  public void oneToOneUpdate_EntityGetsUpdatedCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    updateOwningSide_SetNewInverseSide(owningSide);
    underTest.update(owningSide);

    Dao inverseSideDao = daoCache.getDaoForEntity(OneToOneInverseEntity.class);
    inverseSideDao.update(owningSide.getInverseSide());
    inverseSideDao.update(inverseSide);

    Document persistedOwningSideDocument = database.getExistingDocument(owningSide.getId());
    Assert.assertNotNull(persistedOwningSideDocument);
    Assert.assertNotEquals(inverseSide.getId(), persistedOwningSideDocument.getProperty(OneToOneOwningEntity.INVERSE_SIDE_COLUMN_NAME));
    Assert.assertEquals(owningSide.getInverseSide().getId(), persistedOwningSideDocument.getProperty(OneToOneOwningEntity.INVERSE_SIDE_COLUMN_NAME));

    Document persistedNewInverseSideDocument = database.getExistingDocument(owningSide.getInverseSide().getId());
    Assert.assertNotNull(persistedNewInverseSideDocument);
    Assert.assertEquals(owningSide.getId(), persistedNewInverseSideDocument.getProperty(OneToOneInverseEntity.OWNING_SIDE_COLUMN_NAME));

    // previous inverse side didn't get deleted
    Document persistedPreviousInverseSideDocument = database.getExistingDocument(inverseSide.getId());
    Assert.assertNotNull(persistedPreviousInverseSideDocument);
    Assert.assertNotEquals(owningSide.getId(), persistedPreviousInverseSideDocument.getProperty(OneToOneInverseEntity.OWNING_SIDE_COLUMN_NAME));
  }

  @Test
  public void oneToOneUpdate_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    Date owningSideModifiedOnBeforeDeletion = owningSide.getModifiedOn();
    Date inverseSideModifiedOnBeforeDeletion = inverseSide.getModifiedOn();

    updateOwningSide_SetNewInverseSide(owningSide);
    underTest.update(owningSide);

    Assert.assertNotNull(owningSide.getId());
    Assert.assertNotNull(owningSide.getVersion());
    Assert.assertEquals(3L, (long)owningSide.getVersion());
    Assert.assertNotNull(owningSide.getCreatedOn());
    Assert.assertNotEquals(owningSide.getCreatedOn(), owningSide.getModifiedOn());
    Assert.assertNotNull(owningSide.getModifiedOn());
    Assert.assertNotEquals(owningSideModifiedOnBeforeDeletion, owningSide.getModifiedOn());

    // assert inverse side hasn't been updated
    Assert.assertNotNull(inverseSide.getId());
    Assert.assertNotNull(inverseSide.getVersion());
    Assert.assertEquals(1L, (long)inverseSide.getVersion());
    Assert.assertNotNull(inverseSide.getCreatedOn());
    Assert.assertEquals(inverseSide.getCreatedOn(), inverseSide.getModifiedOn());
    Assert.assertNotNull(inverseSide.getModifiedOn());
    Assert.assertEquals(inverseSideModifiedOnBeforeDeletion, inverseSide.getModifiedOn());
  }

  @Test
  public void oneToOneUpdate_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    updateOwningSide_SetNewInverseSide(owningSide);
    underTest.update(owningSide);

    Assert.assertTrue(owningSide.hasPrePersistBeenCalled());
    Assert.assertTrue(owningSide.hasPostPersistBeenCalled());
    Assert.assertFalse(owningSide.hasPostLoadBeenCalled());
    Assert.assertTrue(owningSide.hasPreUpdateBeenCalled());
    Assert.assertTrue(owningSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(owningSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(owningSide.hasPostRemoveBeenCalled());

    // assert inverse side hasn't been updated
    Assert.assertTrue(inverseSide.hasPrePersistBeenCalled());
    Assert.assertTrue(inverseSide.hasPostPersistBeenCalled());
    Assert.assertFalse(inverseSide.hasPostLoadBeenCalled());
    Assert.assertFalse(inverseSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(inverseSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(inverseSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(inverseSide.hasPostRemoveBeenCalled());
  }


  @Test
  public void oneToOneDelete_EntityGetsDeletedCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    Document persistedDocumentBefore = database.getExistingDocument(owningSide.getId());
    Assert.assertNotNull(persistedDocumentBefore);

    underTest.delete(owningSide);

    Document persistedOwningSideDocument = database.getDocument(owningSide.getId());
    Assert.assertTrue(persistedOwningSideDocument.isDeleted());

    Document persistedOwningSideExistingDocument = database.getExistingDocument(owningSide.getId());
    Assert.assertNull(persistedOwningSideExistingDocument); // null means it doesn't exist

    Document persistedInverseSideDocument = database.getDocument(owningSide.getId());
    Assert.assertTrue(persistedInverseSideDocument.isDeleted());

    Document persistedInverseSideExistingDocument = database.getExistingDocument(inverseSide.getId());
    Assert.assertNull(persistedInverseSideExistingDocument); // null means it doesn't exist
  }

  @Test
  public void oneToOneDelete_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    Date owningSideModifiedOnBeforeDeletion = owningSide.getModifiedOn();
    Date inverseSideModifiedOnBeforeDeletion = inverseSide.getModifiedOn();

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
    Assert.assertNotNull(inverseSide.getId());
    Assert.assertNull(inverseSide.getVersion());
    Assert.assertNotNull(inverseSide.getCreatedOn());
    Assert.assertNotEquals(inverseSide.getCreatedOn(), inverseSide.getModifiedOn());
    Assert.assertNotNull(inverseSide.getModifiedOn());
    Assert.assertNotEquals(inverseSideModifiedOnBeforeDeletion, inverseSide.getModifiedOn());
  }

  @Test
  public void oneToOneDelete_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

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
    Assert.assertTrue(inverseSide.hasPrePersistBeenCalled());
    Assert.assertTrue(inverseSide.hasPostPersistBeenCalled());
    Assert.assertFalse(inverseSide.hasPostLoadBeenCalled());
    Assert.assertFalse(inverseSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(inverseSide.hasPostUpdateBeenCalled());
    Assert.assertTrue(inverseSide.hasPreRemoveBeenCalled());
    Assert.assertTrue(inverseSide.hasPostRemoveBeenCalled());
  }


  protected void updateOwningSide_SetNewInverseSide(OneToOneOwningEntity owningSide) throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity newInverseSide = new OneToOneInverseEntity();

    Dao inverseSideDao = daoCache.getDaoForEntity(OneToOneInverseEntity.class);
    inverseSideDao.create(newInverseSide);

    owningSide.setInverseSide(newInverseSide);
  }

}

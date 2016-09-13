package net.dankito.jpa.couchbaselite.unreleated;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import net.dankito.jpa.couchbaselite.DaoTestBase;
import net.dankito.jpa.couchbaselite.testmodel.EntityWithAllDataTypes;
import net.dankito.jpa.couchbaselite.testmodel.ManyToManyOwningSide;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Date;

/**
 * Created by ganymed on 15/08/16.
 */
public class NonRelationshipEntityDaoTest extends DaoTestBase {

  protected static final String TEST_ENTITY_NAME = "Mahatma Gandhi";
  protected static final String TEST_ENTITY_NAME_AFTER_UPDATE = "Mother Teresa";

  protected static final int TEST_ENTITY_AGE = 78;
  protected static final int TEST_ENTITY_AGE_AFTER_UPDATE = 87;

  protected static final Date TEST_ENTITY_DAY_OF_BIRTH = new Date(-31, 9, 2);
  protected static final Date TEST_ENTITY_DAY_OF_BIRTH_AFTER_UPDATE = new Date(10, 7, 26);

  protected static final boolean TEST_ENTITY_IS_MARRIED = true;
  protected static final boolean TEST_ENTITY_IS_MARRIED_AFTER_UPDATE = false;


  @Override
  protected Class[] getEntitiesToTest() {
    return new Class[] { EntityWithAllDataTypes.class };
  }


  @Test(expected = SQLException.class)
  public void persistNull_ThrowsException() throws CouchbaseLiteException, SQLException {
    underTest.create(null);
  }


  @Test(expected = SQLException.class)
  public void persistOtherThanDaosEntity_ThrowsException() throws CouchbaseLiteException, SQLException {
    underTest.create(new ManyToManyOwningSide());
  }


  @Test
  public void persistEntity_AllPropertiesGetPersistedCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();

    underTest.create(testEntity);

    Document persistedDocument = database.getDocument(testEntity.getId());
    Assert.assertNotNull(persistedDocument);

    Assert.assertEquals(TEST_ENTITY_NAME, persistedDocument.getProperty(EntityWithAllDataTypes.NAME_COLUMN_NAME));
    Assert.assertEquals(TEST_ENTITY_AGE, persistedDocument.getProperty(EntityWithAllDataTypes.AGE_COLUMN_NAME));
    Assert.assertEquals(TEST_ENTITY_DAY_OF_BIRTH.getTime(), persistedDocument.getProperty(EntityWithAllDataTypes.DAY_OF_BIRTH_COLUMN_NAME));
    Assert.assertEquals(TEST_ENTITY_IS_MARRIED, persistedDocument.getProperty(EntityWithAllDataTypes.IS_MARRIED_COLUMN_NAME));
  }

  @Test
  public void persistEntity_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();

    underTest.create(testEntity);

    Assert.assertNotNull(testEntity.getId());
    Assert.assertNotNull(testEntity.getVersion());
    Assert.assertEquals(1L, (long)testEntity.getVersion());
    Assert.assertNotNull(testEntity.getCreatedOn());
    Assert.assertNotNull(testEntity.getModifiedOn());
    Assert.assertEquals(testEntity.getCreatedOn(), testEntity.getModifiedOn());
  }

  @Test
  public void persistEntity_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();

    underTest.create(testEntity);

    Assert.assertTrue(testEntity.hasPrePersistBeenCalled());
    Assert.assertTrue(testEntity.hasPostPersistBeenCalled());
    Assert.assertFalse(testEntity.hasPostLoadBeenCalled());
    Assert.assertFalse(testEntity.hasPreUpdateBeenCalled());
    Assert.assertFalse(testEntity.hasPostUpdateBeenCalled());
    Assert.assertFalse(testEntity.hasPreRemoveBeenCalled());
    Assert.assertFalse(testEntity.hasPostRemoveBeenCalled());
  }


  @Test
  public void retrieveEntity_AllPropertiesAreSetCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();
    underTest.create(testEntity);

    objectCache.clear();

    EntityWithAllDataTypes persistedEntity = (EntityWithAllDataTypes)underTest.retrieve(testEntity.getId());

    Assert.assertEquals(TEST_ENTITY_NAME, persistedEntity.getName());
    Assert.assertEquals(TEST_ENTITY_AGE, persistedEntity.getAge());
    Assert.assertEquals(TEST_ENTITY_DAY_OF_BIRTH, persistedEntity.getDayOfBirth());
    Assert.assertEquals(TEST_ENTITY_IS_MARRIED, persistedEntity.isMarried());
  }

  @Test
  public void retrieveEntity_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();
    underTest.create(testEntity);

    objectCache.clear();

    EntityWithAllDataTypes persistedEntity = (EntityWithAllDataTypes)underTest.retrieve(testEntity.getId());

    Assert.assertNotNull(persistedEntity.getId());
    Assert.assertNotNull(persistedEntity.getVersion());
    Assert.assertEquals(1L, (long)persistedEntity.getVersion());
    Assert.assertNotNull(persistedEntity.getCreatedOn());
    Assert.assertNotNull(persistedEntity.getModifiedOn());
  }

  @Test
  public void retrieveEntity_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();
    underTest.create(testEntity);

    objectCache.clear();

    EntityWithAllDataTypes persistedEntity = (EntityWithAllDataTypes)underTest.retrieve(testEntity.getId());

    Assert.assertFalse(persistedEntity.hasPrePersistBeenCalled());
    Assert.assertFalse(persistedEntity.hasPostPersistBeenCalled());
    Assert.assertTrue(persistedEntity.hasPostLoadBeenCalled());
    Assert.assertFalse(persistedEntity.hasPreUpdateBeenCalled());
    Assert.assertFalse(persistedEntity.hasPostUpdateBeenCalled());
    Assert.assertFalse(persistedEntity.hasPreRemoveBeenCalled());
    Assert.assertFalse(persistedEntity.hasPostRemoveBeenCalled());
  }


  @Test
  public void updateEntity_AllPropertiesGetUpdatedCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();
    underTest.create(testEntity);

    updateTestEntity(testEntity);
    underTest.update(testEntity);

    Document persistedDocument = database.getDocument(testEntity.getId());
    Assert.assertNotNull(persistedDocument);

    Assert.assertEquals(TEST_ENTITY_NAME_AFTER_UPDATE, persistedDocument.getProperty(EntityWithAllDataTypes.NAME_COLUMN_NAME));
    Assert.assertEquals(TEST_ENTITY_AGE_AFTER_UPDATE, persistedDocument.getProperty(EntityWithAllDataTypes.AGE_COLUMN_NAME));
    Assert.assertEquals(TEST_ENTITY_DAY_OF_BIRTH_AFTER_UPDATE.getTime(), persistedDocument.getProperty(EntityWithAllDataTypes.DAY_OF_BIRTH_COLUMN_NAME));
    Assert.assertEquals(TEST_ENTITY_IS_MARRIED_AFTER_UPDATE, persistedDocument.getProperty(EntityWithAllDataTypes.IS_MARRIED_COLUMN_NAME));
  }

  @Test
  public void updateEntity_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();
    underTest.create(testEntity);

    Date modifiedOnBeforeUpdate = testEntity.getModifiedOn();

    updateTestEntity(testEntity);
    underTest.update(testEntity);

    Assert.assertNotNull(testEntity.getId());
    Assert.assertNotNull(testEntity.getVersion());
    Assert.assertEquals(2L, (long)testEntity.getVersion());
    Assert.assertNotNull(testEntity.getCreatedOn());
    Assert.assertNotEquals(testEntity.getCreatedOn(), testEntity.getModifiedOn());
    Assert.assertNotNull(testEntity.getModifiedOn());
    Assert.assertNotEquals(modifiedOnBeforeUpdate, testEntity.getModifiedOn());
  }

  @Test
  public void updateEntity_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();
    underTest.create(testEntity);

    updateTestEntity(testEntity);
    underTest.update(testEntity);

    Assert.assertTrue(testEntity.hasPrePersistBeenCalled());
    Assert.assertTrue(testEntity.hasPostPersistBeenCalled());
    Assert.assertFalse(testEntity.hasPostLoadBeenCalled());
    Assert.assertTrue(testEntity.hasPreUpdateBeenCalled());
    Assert.assertTrue(testEntity.hasPostUpdateBeenCalled());
    Assert.assertFalse(testEntity.hasPreRemoveBeenCalled());
    Assert.assertFalse(testEntity.hasPostRemoveBeenCalled());
  }


  @Test
  public void deleteEntity_EntityGetsDeletedCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();
    underTest.create(testEntity);

    Document persistedDocumentBefore = database.getExistingDocument(testEntity.getId());
    Assert.assertNotNull(persistedDocumentBefore);

    underTest.delete(testEntity);

    Document document = database.getDocument(testEntity.getId());
    Assert.assertTrue(document.isDeleted());

    Document persistedDocument = database.getExistingDocument(testEntity.getId());
    Assert.assertNull(persistedDocument); // null means it doesn't exist
  }

  @Test
  public void deleteEntity_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();
    underTest.create(testEntity);

    Date modifiedOnBeforeDeletion = testEntity.getModifiedOn();

    Document persistedDocumentBefore = database.getExistingDocument(testEntity.getId());
    Assert.assertNotNull(persistedDocumentBefore);

    underTest.delete(testEntity);

    Assert.assertNotNull(testEntity.getId());
    Assert.assertNull(testEntity.getVersion());
    Assert.assertNotNull(testEntity.getCreatedOn());
    Assert.assertNotEquals(testEntity.getCreatedOn(), testEntity.getModifiedOn());
    Assert.assertNotNull(testEntity.getModifiedOn());
    Assert.assertNotEquals(modifiedOnBeforeDeletion, testEntity.getModifiedOn());
  }

  @Test
  public void deleteEntity_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();
    underTest.create(testEntity);

    Document persistedDocumentBefore = database.getExistingDocument(testEntity.getId());
    Assert.assertNotNull(persistedDocumentBefore);

    underTest.delete(testEntity);

    Assert.assertTrue(testEntity.hasPrePersistBeenCalled());
    Assert.assertTrue(testEntity.hasPostPersistBeenCalled());
    Assert.assertFalse(testEntity.hasPostLoadBeenCalled());
    Assert.assertFalse(testEntity.hasPreUpdateBeenCalled());
    Assert.assertFalse(testEntity.hasPostUpdateBeenCalled());
    Assert.assertTrue(testEntity.hasPreRemoveBeenCalled());
    Assert.assertTrue(testEntity.hasPostRemoveBeenCalled());
  }



  protected EntityWithAllDataTypes createTestEntity() {
    EntityWithAllDataTypes testEntity = new EntityWithAllDataTypes();

    testEntity.setName(TEST_ENTITY_NAME);
    testEntity.setAge(TEST_ENTITY_AGE);
    testEntity.setDayOfBirth(TEST_ENTITY_DAY_OF_BIRTH);
    testEntity.setMarried(TEST_ENTITY_IS_MARRIED);

    return testEntity;
  }

  protected void updateTestEntity(EntityWithAllDataTypes testEntity) {
    testEntity.setName(TEST_ENTITY_NAME_AFTER_UPDATE);
    testEntity.setAge(TEST_ENTITY_AGE_AFTER_UPDATE);
    testEntity.setDayOfBirth(TEST_ENTITY_DAY_OF_BIRTH_AFTER_UPDATE);
    testEntity.setMarried(TEST_ENTITY_IS_MARRIED_AFTER_UPDATE);
  }


}

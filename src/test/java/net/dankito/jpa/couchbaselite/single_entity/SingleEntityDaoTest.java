package net.dankito.jpa.couchbaselite.single_entity;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import net.dankito.jpa.annotationreader.JpaEntityConfigurationReader;
import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.couchbaselite.DaoTestBase;
import net.dankito.jpa.couchbaselite.testmodel.EntityWithAllDataTypes;
import net.dankito.jpa.couchbaselite.testmodel.ManyToManyOwningSide;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Date;

/**
 * Created by ganymed on 15/08/16.
 */
public class SingleEntityDaoTest extends DaoTestBase {

  protected static final String TEST_ENTITY_NAME = "Mahatma Gandhi";
  protected static final String TEST_ENTITY_NAME_AFTER_UPDATE = "Mother Teresa";

  protected static final int TEST_ENTITY_AGE = 78;
  protected static final int TEST_ENTITY_AGE_AFTER_UPDATE = 87;

  protected static final Date TEST_ENTITY_DAY_OF_BIRTH = new Date(-31, 9, 2);
  protected static final Date TEST_ENTITY_DAY_OF_BIRTH_AFTER_UPDATE = new Date(10, 7, 26);

  protected static final boolean TEST_ENTITY_IS_MARRIED = true;
  protected static final boolean TEST_ENTITY_IS_MARRIED_AFTER_UPDATE = false;


  protected EntityConfig entityConfig;

  @Before
  public void setUp() throws Exception {
    EntityConfig[] readEntities = new JpaEntityConfigurationReader().readConfiguration(EntityWithAllDataTypes.class);
    entityConfig = readEntities[0];

    setUpDatabaseAndDao(entityConfig);
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

    Assert.assertNotNull(testEntity.getId());
    Assert.assertNotNull(testEntity.getVersion());
    Assert.assertTrue(testEntity.getVersion().startsWith("1"));
    Assert.assertNotNull(testEntity.getCreatedOn());
    Assert.assertNotNull(testEntity.getModifiedOn());

    Document persistedDocument = database.getDocument(testEntity.getId());
    Assert.assertNotNull(persistedDocument);

    Assert.assertEquals(TEST_ENTITY_NAME, persistedDocument.getProperty(EntityWithAllDataTypes.NAME_COLUMN_NAME));
    Assert.assertEquals(TEST_ENTITY_AGE, persistedDocument.getProperty(EntityWithAllDataTypes.AGE_COLUMN_NAME));
    Assert.assertEquals(TEST_ENTITY_DAY_OF_BIRTH.getTime(), persistedDocument.getProperty(EntityWithAllDataTypes.DAY_OF_BIRTH_COLUMN_NAME));
    Assert.assertEquals(TEST_ENTITY_IS_MARRIED, persistedDocument.getProperty(EntityWithAllDataTypes.IS_MARRIED_COLUMN_NAME));
  }


  @Test
  public void retrieveEntity_AllPropertiesAreSetCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();
    underTest.create(testEntity);

    // TODO: turn off Cache
    EntityWithAllDataTypes persistedEntity = (EntityWithAllDataTypes)underTest.retrieve(testEntity.getId());

    Assert.assertNotNull(persistedEntity.getId());
    Assert.assertNotNull(persistedEntity.getVersion());
    Assert.assertTrue(persistedEntity.getVersion().startsWith("1"));
    Assert.assertNotNull(persistedEntity.getCreatedOn());
    Assert.assertNotNull(persistedEntity.getModifiedOn());

    Assert.assertEquals(TEST_ENTITY_NAME, persistedEntity.getName());
    Assert.assertEquals(TEST_ENTITY_AGE, persistedEntity.getAge());
    Assert.assertEquals(TEST_ENTITY_DAY_OF_BIRTH, persistedEntity.getDayOfBirth());
    Assert.assertEquals(TEST_ENTITY_IS_MARRIED, persistedEntity.isMarried());
  }


  @Test
  public void updateEntity_AllPropertiesGetUpdatedCorrectly() throws CouchbaseLiteException, SQLException {
    EntityWithAllDataTypes testEntity = createTestEntity();
    underTest.create(testEntity);

    updateTestEntity(testEntity);
    underTest.update(testEntity);

    Assert.assertNotNull(testEntity.getId());
    Assert.assertNotNull(testEntity.getVersion());
    Assert.assertTrue(testEntity.getVersion().startsWith("2"));
    Assert.assertNotNull(testEntity.getCreatedOn());
    Assert.assertNotNull(testEntity.getModifiedOn());

    Document persistedDocument = database.getDocument(testEntity.getId());
    Assert.assertNotNull(persistedDocument);

    Assert.assertEquals(TEST_ENTITY_NAME_AFTER_UPDATE, persistedDocument.getProperty(EntityWithAllDataTypes.NAME_COLUMN_NAME));
    Assert.assertEquals(TEST_ENTITY_AGE_AFTER_UPDATE, persistedDocument.getProperty(EntityWithAllDataTypes.AGE_COLUMN_NAME));
    Assert.assertEquals(TEST_ENTITY_DAY_OF_BIRTH_AFTER_UPDATE, persistedDocument.getProperty(EntityWithAllDataTypes.DAY_OF_BIRTH_COLUMN_NAME));
    Assert.assertEquals(TEST_ENTITY_IS_MARRIED_AFTER_UPDATE, persistedDocument.getProperty(EntityWithAllDataTypes.IS_MARRIED_COLUMN_NAME));
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

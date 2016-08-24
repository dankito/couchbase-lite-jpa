package net.dankito.jpa.couchbaselite.inheritance;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Document;
import com.couchbase.lite.JavaContext;
import com.couchbase.lite.Manager;

import net.dankito.jpa.annotationreader.JpaEntityConfigurationReader;
import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.util.ConfigRegistry;
import net.dankito.jpa.cache.ObjectCache;
import net.dankito.jpa.cache.RelationshipDaoCache;
import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.couchbaselite.testmodel.enums.Gender;
import net.dankito.jpa.couchbaselite.testmodel.inheritance.JoinTableChild_1;
import net.dankito.jpa.couchbaselite.testmodel.inheritance.JoinTableChild_2_1;
import net.dankito.jpa.couchbaselite.testmodel.inheritance.JoinTableChild_2_2;
import net.dankito.jpa.couchbaselite.testmodel.inheritance.JoinTableChild_2_MappedSuperclass;
import net.dankito.jpa.couchbaselite.testmodel.inheritance.JoinTableChild_3;
import net.dankito.jpa.couchbaselite.testmodel.inheritance.JoinedTableBase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ganymed on 24/08/16.
 */
public class JoinedInheritanceDaoTest {

  public static final String CHILD_1_NAME = "1";
  public static final Date CHILD_1_DAY_OF_BIRTH = new Date(88, 2, 27);

  public static final String CHILD_2_1_NAME = "2_1";
  public static final String CHILD_2_1_GIVEN_NAME = "Hubertus";
  public static final double CHILD_2_1_HEIGHT = 1.75;

  public static final String CHILD_2_2_NAME = "2_2";
  public static final String CHILD_2_2_GIVEN_NAME = "Frederick";
  public static final BigDecimal CHILD_2_2_SALARY = new BigDecimal(2.50);

  public static final String CHILD_3_NAME = "3";
  public static final Gender CHILD_3_GENDER = Gender.FEMALE;


  protected Dao joinedTableDao;
  protected Dao joinChild_1_Dao;
  protected Dao joinChild_2_1_Dao;
  protected Dao joinChild_2_2_Dao;
  protected Dao joinChild_3_Dao;

  protected Database database;

  protected File tempDbPath;

  protected ObjectCache objectCache;

  protected RelationshipDaoCache relationshipDaoCache;


  @Before
  public void setUp() throws Exception {
    ConfigRegistry configRegistry = new ConfigRegistry();
    new JpaEntityConfigurationReader().readConfiguration(configRegistry, JoinTableChild_1.class, JoinTableChild_2_1.class,
                                                                              JoinTableChild_2_2.class, JoinTableChild_3.class);

    setUpDatabase();

    objectCache = new ObjectCache();
    relationshipDaoCache = new RelationshipDaoCache();

    createDao(configRegistry);
  }

  protected void createDao(ConfigRegistry configRegistry) {
    EntityConfig joinedTableBaseConfig = configRegistry.getEntityConfiguration(JoinedTableBase.class);
    joinedTableDao = new Dao(database, joinedTableBaseConfig, objectCache, relationshipDaoCache);
    relationshipDaoCache.addDao(joinedTableBaseConfig.getEntityClass(), joinedTableDao);

    EntityConfig child1Config = configRegistry.getEntityConfiguration(JoinTableChild_1.class);
    joinChild_1_Dao = new Dao(database, child1Config, objectCache, relationshipDaoCache);
    relationshipDaoCache.addDao(child1Config.getEntityClass(), joinChild_1_Dao);

    EntityConfig child2_1Config = configRegistry.getEntityConfiguration(JoinTableChild_2_1.class);
    joinChild_2_1_Dao = new Dao(database, child2_1Config, objectCache, relationshipDaoCache);
    relationshipDaoCache.addDao(child2_1Config.getEntityClass(), joinChild_2_1_Dao);

    EntityConfig child2_2Config = configRegistry.getEntityConfiguration(JoinTableChild_2_2.class);
    joinChild_2_2_Dao = new Dao(database, child2_2Config, objectCache, relationshipDaoCache);
    relationshipDaoCache.addDao(child2_2Config.getEntityClass(), joinChild_2_2_Dao);

    EntityConfig child3Config = configRegistry.getEntityConfiguration(JoinTableChild_3.class);
    joinChild_3_Dao = new Dao(database, child3Config, objectCache, relationshipDaoCache);
    relationshipDaoCache.addDao(child3Config.getEntityClass(), joinChild_3_Dao);
  }

  protected void setUpDatabase() throws Exception {
    tempDbPath = Files.createTempDirectory("couchbase_lite_test").toFile();
    tempDbPath.deleteOnExit();

    Manager manager = new Manager(new JavaContext(), Manager.DEFAULT_OPTIONS);
    DatabaseOptions options = new DatabaseOptions();
    options.setCreate(true);
    database = manager.openDatabase("test_db", options);
  }


  @After
  public void tearDown() {
    tempDbPath.delete();
  }



  @Test
  public void createJoined_AllPropertiesGetPersistedCorrectly() throws CouchbaseLiteException, SQLException {
    JoinTableChild_1 joinTableChild_1 = createTestChild_1_Entity();
    JoinTableChild_2_1 joinTableChild_2_1 = createTestChild_2_1_Entity();
    JoinTableChild_2_2 joinTableChild_2_2 = createTestChild_2_2_Entity();
    JoinTableChild_3 joinTableChild_3 = createTestChild_3_Entity();

    Document persistedChild_1_Document = database.getDocument(joinTableChild_1.getId());
    Assert.assertNotNull(persistedChild_1_Document);
    Assert.assertEquals(CHILD_1_DAY_OF_BIRTH.getTime(), persistedChild_1_Document.getProperty(JoinTableChild_1.DAY_OF_BIRTH_COLUMN_NAME));

    Document persistedChild_1_ParentDocument = database.getDocument((String)persistedChild_1_Document.getProperty(Dao.PARENT_DOCUMENT_ID_COLUMN_NAME));
    Assert.assertEquals(CHILD_1_NAME, persistedChild_1_ParentDocument.getProperty(JoinedTableBase.NAME_COLUMN_NAME));

    Document persistedChild_2_1_Document = database.getDocument(joinTableChild_2_1.getId());
    Assert.assertNotNull(persistedChild_2_1_Document);
    Assert.assertEquals(CHILD_2_1_GIVEN_NAME, persistedChild_2_1_Document.getProperty(JoinTableChild_2_MappedSuperclass.GIVEN_NAME_COLUMN_NAME));
    Assert.assertEquals(CHILD_2_1_HEIGHT, persistedChild_2_1_Document.getProperty(JoinTableChild_2_1.HEIGHT_COLUMN_NAME));

    Document persistedChild_2_1_ParentDocument = database.getDocument((String)persistedChild_2_1_Document.getProperty(Dao.PARENT_DOCUMENT_ID_COLUMN_NAME));
    Assert.assertEquals(CHILD_2_1_NAME, persistedChild_2_1_ParentDocument.getProperty(JoinedTableBase.NAME_COLUMN_NAME));

    Document persistedChild_2_2_Document = database.getDocument(joinTableChild_2_2.getId());
    Assert.assertNotNull(persistedChild_2_2_Document);
    Assert.assertEquals(CHILD_2_2_GIVEN_NAME, persistedChild_2_2_Document.getProperty(JoinTableChild_2_MappedSuperclass.GIVEN_NAME_COLUMN_NAME));
    Assert.assertEquals(CHILD_2_2_SALARY.doubleValue(), persistedChild_2_2_Document.getProperty(JoinTableChild_2_2.SALARY_COLUMN_NAME));

    Document persistedChild_2_2_ParentDocument = database.getDocument((String)persistedChild_2_2_Document.getProperty(Dao.PARENT_DOCUMENT_ID_COLUMN_NAME));
    Assert.assertEquals(CHILD_2_2_NAME, persistedChild_2_2_ParentDocument.getProperty(JoinedTableBase.NAME_COLUMN_NAME));

    Document persistedChild_3_Document = database.getDocument(joinTableChild_3.getId());
    Assert.assertNotNull(persistedChild_3_Document);
    Assert.assertEquals(CHILD_3_GENDER.toString(), persistedChild_3_Document.getProperty(JoinTableChild_3.GENDER_COLUMN_NAME));

    Document persistedChild_3_ParentDocument = database.getDocument((String)persistedChild_3_Document.getProperty(Dao.PARENT_DOCUMENT_ID_COLUMN_NAME));
    Assert.assertEquals(CHILD_3_NAME, persistedChild_3_ParentDocument.getProperty(JoinedTableBase.NAME_COLUMN_NAME));
  }

  @Test
  public void createJoined_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    List<JoinedTableBase> testEntities = createTestEntities();

    for(JoinedTableBase testEntity : testEntities) {
      Assert.assertNotNull(testEntity.getId());
      Assert.assertNotNull(testEntity.getVersion());
      Assert.assertTrue(testEntity.getVersion().startsWith("1"));
      Assert.assertNotNull(testEntity.getCreatedOn());
      Assert.assertNotNull(testEntity.getModifiedOn());
      Assert.assertEquals(testEntity.getCreatedOn(), testEntity.getModifiedOn());
    }
  }

  @Test
  public void createJoined_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    List<JoinedTableBase> testEntities = createTestEntities();

    for(JoinedTableBase testEntity : testEntities) {
      Assert.assertTrue(testEntity.hasPrePersistBeenCalled());
      Assert.assertTrue(testEntity.hasPostPersistBeenCalled());
      Assert.assertFalse(testEntity.hasPostLoadBeenCalled());
      Assert.assertFalse(testEntity.hasPreUpdateBeenCalled());
      Assert.assertFalse(testEntity.hasPostUpdateBeenCalled());
      Assert.assertFalse(testEntity.hasPreRemoveBeenCalled());
      Assert.assertFalse(testEntity.hasPostRemoveBeenCalled());
    }
  }


  protected List<JoinedTableBase> createTestEntities() throws CouchbaseLiteException, SQLException {
    List<JoinedTableBase> testEntities = new ArrayList<>();

    testEntities.add(createTestChild_1_Entity());
    testEntities.add(createTestChild_2_1_Entity());
    testEntities.add(createTestChild_2_2_Entity());
    testEntities.add(createTestChild_3_Entity());

    return testEntities;
  }

  protected JoinTableChild_1 createTestChild_1_Entity() throws CouchbaseLiteException, SQLException {
    JoinTableChild_1 testEntity = new JoinTableChild_1(CHILD_1_NAME, CHILD_1_DAY_OF_BIRTH);

    joinChild_1_Dao.create(testEntity);

    return testEntity;
  }

  protected JoinTableChild_2_1 createTestChild_2_1_Entity() throws CouchbaseLiteException, SQLException {
    JoinTableChild_2_1 testEntity = new JoinTableChild_2_1(CHILD_2_1_NAME, CHILD_2_1_GIVEN_NAME, CHILD_2_1_HEIGHT);

    joinChild_2_1_Dao.create(testEntity);

    return testEntity;
  }

  protected JoinTableChild_2_2 createTestChild_2_2_Entity() throws CouchbaseLiteException, SQLException {
    JoinTableChild_2_2 testEntity = new JoinTableChild_2_2(CHILD_2_2_NAME, CHILD_2_2_GIVEN_NAME, CHILD_2_2_SALARY);

    joinChild_2_2_Dao.create(testEntity);

    return testEntity;
  }

  protected JoinTableChild_3 createTestChild_3_Entity() throws CouchbaseLiteException, SQLException {
    JoinTableChild_3 testEntity = new JoinTableChild_3(CHILD_3_NAME, CHILD_3_GENDER);

    joinChild_3_Dao.create(testEntity);

    return testEntity;
  }
}
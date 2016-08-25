package net.dankito.jpa.couchbaselite;

import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.JavaContext;
import com.couchbase.lite.Manager;

import net.dankito.jpa.annotationreader.JpaEntityConfigurationReader;
import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.cache.ObjectCache;
import net.dankito.jpa.cache.DaoCache;
import net.dankito.jpa.util.IValueConverter;
import net.dankito.jpa.util.ValueConverter;

import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.nio.file.Files;

/**
 * Created by ganymed on 15/08/16.
 */
public abstract class DaoTestBase {

  protected Dao underTest;

  protected EntityConfig entityConfig;

  protected Database database;

  protected File tempDbPath;

  protected ObjectCache objectCache;

  protected DaoCache daoCache;

  protected IValueConverter valueConverter;


  @Before
  public void setUp() throws Exception {
    EntityConfig[] readEntities = new JpaEntityConfigurationReader().readConfiguration(getEntitiesToTest());

    setUpDatabase();

    objectCache = new ObjectCache();
    daoCache = new DaoCache();
    valueConverter = new ValueConverter();

    entityConfig = readEntities[0];

    underTest = new Dao(database, entityConfig, objectCache, daoCache, valueConverter);
    daoCache.addDao(entityConfig.getEntityClass(), underTest);

    for(int i = 1; i < readEntities.length; i++) {
      Dao dao = new Dao(database, readEntities[i], objectCache, daoCache, valueConverter);
      daoCache.addDao(readEntities[i].getEntityClass(), dao);
    }
  }

  protected abstract Class[] getEntitiesToTest();


  public void setUpDatabase() throws Exception {
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

}

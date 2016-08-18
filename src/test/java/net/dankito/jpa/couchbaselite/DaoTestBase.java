package net.dankito.jpa.couchbaselite;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.JavaContext;
import com.couchbase.lite.Manager;

import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.cache.ObjectCache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

/**
 * Created by ganymed on 15/08/16.
 */
public class DaoTestBase {

  protected Dao underTest;

  protected Database database;

  protected File tempDbPath;

  protected ObjectCache objectCache;


  public void setUpDatabaseAndDao(EntityConfig entityConfig) throws Exception {
    tempDbPath = Files.createTempDirectory("couchbase_lite_test").toFile();
    tempDbPath.deleteOnExit();

    Manager manager = new Manager(new JavaContext(), Manager.DEFAULT_OPTIONS);
    DatabaseOptions options = new DatabaseOptions();
    options.setCreate(true);
    database = manager.openDatabase("test_db", options);

    objectCache = new ObjectCache();

    underTest = new Dao(database, entityConfig, objectCache);
  }

  @After
  public void tearDown() {
    tempDbPath.delete();
  }

}

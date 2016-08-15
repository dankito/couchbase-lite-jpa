package net.dankito.jpa.couchbaselite;

import com.couchbase.lite.Database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by ganymed on 15/08/16.
 */
public class DaoTest {

  protected Dao underTest;

  protected File tempDbPath;


  @Before
  public void setUp() throws IOException {
    tempDbPath = Files.createTempDirectory("couchbase_lite_test").toFile();
    tempDbPath.deleteOnExit();

    Database database = new Database(tempDbPath.getAbsolutePath(), "db", null, false);
    underTest = new Dao(database);
  }

  @After
  public void tearDown() {
    tempDbPath.delete();
  }


  @Test
  public void persistNull() {
    underTest.persist(null);
  }

}

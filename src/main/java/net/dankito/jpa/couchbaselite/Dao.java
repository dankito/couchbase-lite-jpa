package net.dankito.jpa.couchbaselite;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;

/**
 * Created by ganymed on 15/08/16.
 */
public class Dao {

  protected Database database;


  public Dao(Database database) {
    this.database = database;
  }


  public boolean persist(Object datum) {
    Document newDocument = database.createDocument();

    return false;
  }

}

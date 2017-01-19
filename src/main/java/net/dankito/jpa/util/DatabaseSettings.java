package net.dankito.jpa.util;

import net.dankito.jpa.couchbaselite.Dao;

/**
 * Created by ganymed on 24/08/16.
 */
public class DatabaseSettings {

  public String sanitizeTableName(String extractedTableName) {
    return extractedTableName; // nothing to do here
  }

  public String sanitizeColumnName(String extractedColumnName) {
    if(isCouchbaseLiteSystemColumnName(extractedColumnName)) {
      return "_" + extractedColumnName;
    }

    return extractedColumnName;
  }

  protected boolean isCouchbaseLiteSystemColumnName(String extractedColumnName) {
    switch(extractedColumnName) {
      case Dao.ID_SYSTEM_COLUMN_NAME:
      case Dao.REVISION_SYSTEM_COLUMN_NAME:
      case Dao.REVISIONS_SYSTEM_COLUMN_NAME:
      case Dao.ATTACHMENTS_SYSTEM_COLUMN_NAME:
      case Dao.DELETED_SYSTEM_COLUMN_NAME:
      case Dao.TYPE_COLUMN_NAME: // yeah, actually not a Couchbase default 'Column', but i use it for Storing Entity Name // TODO: create a Constant for it
      default:
        return false;
    }
  }
}

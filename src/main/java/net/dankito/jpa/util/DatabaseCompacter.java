package net.dankito.jpa.util;

import com.couchbase.lite.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;


public class DatabaseCompacter {

  private static final Logger log = LoggerFactory.getLogger(DatabaseCompacter.class);


  protected Database database;

  protected int minimumDelayBetweenTwoCompactRunsMillis;

  protected Timer compactDatabaseTimer = null;


  public DatabaseCompacter(Database database, int minimumDelayBetweenTwoCompactRunsMillis) {
    this.database = database;
    this.minimumDelayBetweenTwoCompactRunsMillis = minimumDelayBetweenTwoCompactRunsMillis;
  }


  public void scheduleCompacting() {
    synchronized(this) {
      if(compactDatabaseTimer == null) {
        compactDatabaseTimer = new Timer("CompactDatabase");

        compactDatabaseTimer.schedule(new TimerTask() {
          @Override
          public void run() {
            synchronized(DatabaseCompacter.this) {
              compactDatabaseTimer = null;
            }

            compactDatabase();
          }
        }, minimumDelayBetweenTwoCompactRunsMillis);
      }
    }
  }

  protected void compactDatabase() {
    log.info("Running compact database ...");

    try {
      database.compact();

      log.info("Done compacting database");
    } catch(Exception e) { log.error("Could not compact database", e); }
  }

}

package net.dankito.jpa.cache;

import net.dankito.jpa.apt.config.ColumnConfig;
import net.dankito.jpa.couchbaselite.Dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DaoCache {

  protected Map<Class, Dao> cache = new ConcurrentHashMap<>();


  public boolean containsDao(Class entityClass) {
    return cache.containsKey(entityClass);
  }

  public boolean containsTargetDaoForRelationshipProperty(ColumnConfig property) {
    return containsDao(property.getTargetEntity().getEntityClass());
  }

  public void addDao(Class entityClass, Dao dao) {
    cache.put(entityClass, dao);
  }

  public Dao getDaoForEntity(Class entityClass) {
    return cache.get(entityClass);
  }

  public Dao getTargetDaoForRelationshipProperty(ColumnConfig property) {
    return getDaoForEntity(property.getTargetEntity().getEntityClass());
  }
}

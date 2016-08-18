package net.dankito.jpa.cache;

import net.dankito.jpa.annotationreader.config.Property;
import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.couchbaselite.Dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganymed on 18/08/16.
 */
public class RelationshipDaoCache {

  protected Map<Class, Dao> cache = new ConcurrentHashMap<>();


  public boolean containsDao(Class entityClass) {
    return cache.containsKey(entityClass);
  }

  public boolean containsTargetDaoForRelationshipProperty(PropertyConfig property) {
    return containsDao(property.getTargetEntityClass());
  }

  public void addDao(Class entityClass, Dao dao) {
    cache.put(entityClass, dao);
  }

  public Dao getDaoForEntity(Class entityClass) {
    return cache.get(entityClass);
  }

  public Dao getTargetDaoForRelationshipProperty(PropertyConfig property) {
    return getDaoForEntity(property.getTargetEntityClass());
  }
}

package net.dankito.jpa.relationship.collections;

import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.couchbaselite.Dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 21/08/16.
 */
public class LazyLoadingEntitiesCollection extends EntitiesCollection {

  private static final Logger log = LoggerFactory.getLogger(LazyLoadingEntitiesCollection.class);


  // we need to put items in a specific order, so i use a List instead of a Set
  protected List<Object> targetEntitiesIds;

  protected Map<Object, Object> cachedEntities;

  protected boolean cacheEntities = true;


  public LazyLoadingEntitiesCollection(Object object, PropertyConfig property, Dao holdingObjectDao, Dao targetDao) throws SQLException {
    super(object, property, holdingObjectDao, targetDao);
  }


  protected void initializeCollection() throws SQLException {
    targetEntitiesIds = new CopyOnWriteArrayList<>();
    cachedEntities = new ConcurrentHashMap<>();

    targetEntitiesIds.addAll(getJoinedEntityIds());
  }


  @Override
  public Object get(int index) {
    Object id = targetEntitiesIds.get(index); // range check is implicitly done here

    Object cachedEntity = cachedEntities.get(id);
    if(cachedEntity != null) {
      return cachedEntity;
    }

    Object retrievedEntity = retrieveAndCacheEntity(id);

    return retrievedEntity;
  }

  protected Object retrieveAndCacheEntity(Object id) {
    try {
      Object retrievedEntity = targetDao.retrieve(id);

      cacheEntity(id, retrievedEntity);

      return retrievedEntity;
    } catch(Exception e) {
      log.error("Cannot retrieve Target Entity of Type " + property.getTargetEntityClass() + " with id " + id, e);
    }

    return null;
  }


  protected boolean itemAddedToCollection(int index, Object element) {
    try {
      Object id = targetDao.getObjectId(element);

      targetEntitiesIds.add(index, id);

      cacheEntity(id, element);

      return true;
    } catch(Exception e) {
      log.error("Could not add item " + element + " to Collection of Property " + property, e);
    }

    return false;
  }

  protected boolean itemRemovedFromCollection(Object object) {
    try {
      Object id = targetDao.getObjectId(object);

      boolean result = targetEntitiesIds.remove(id);

      removeEntityFromCache(id, object);

      return result;
    } catch(Exception e) {
      log.error("Could not remove Entity " + object + " from Property " + property, e);
    }

    return false;
  }


  protected void cacheEntity(Object id, Object entity) {
    if(cacheEntities) {
      cachedEntities.put(id, entity);
    }
  }

  protected boolean removeEntityFromCache(Object id, Object object) {
    return cachedEntities.remove(id) != null;
  }


  @Override
  public int size() {
    return targetEntitiesIds.size();
  }

  @Override
  public void clear() {
    targetEntitiesIds.clear();
    cachedEntities.clear();
  }

}

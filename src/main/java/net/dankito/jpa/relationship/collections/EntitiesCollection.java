package net.dankito.jpa.relationship.collections;

import net.dankito.jpa.apt.config.ColumnConfig;
import net.dankito.jpa.couchbaselite.Dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class EntitiesCollection extends AbstractList implements Set {

  private static final Logger log = LoggerFactory.getLogger(EntitiesCollection.class);


  // we need to put items in a specific order, so i use a List instead of a Set
  protected List items = new CopyOnWriteArrayList();

  // we need to put items in a specific order, so i use a List instead of a Set
  protected List<Object> targetEntitiesIds;

  protected Map<Object, Object> unpersistedEntities = new ConcurrentHashMap<>();

  protected Object holdingObject; // TODO: find a better name

  protected ColumnConfig property;

  protected Dao holdingObjectDao;

  protected Dao targetDao;


  public EntitiesCollection(Object object, ColumnConfig property, Dao holdingObjectDao, Dao targetDao, Collection<Object> targetEntitiesIds) throws SQLException {
    this.holdingObject = object;
    this.property = property;
    this.holdingObjectDao = holdingObjectDao;
    this.targetDao = targetDao;
    this.targetEntitiesIds = new CopyOnWriteArrayList(targetEntitiesIds);

    initializeCollection(this.targetEntitiesIds);
  }


  protected void initializeCollection(Collection<Object> targetEntitiesIds) throws SQLException {
    if(targetEntitiesIds == null) { // TODO: can never happen anymore as this.targetEntitiesIds is passed to initializeCollection()
      targetEntitiesIds = getJoinedEntityIds(); // TODO: this won't set field this.targetEntitiesIds !
    }

    retrievedTargetEntities(targetEntitiesIds);
  }

  protected void retrievedTargetEntities(Collection<Object> targetEntitiesIds) throws SQLException {
    addAll(targetDao.retrieve(targetEntitiesIds));
  }

  protected Collection<Object> getJoinedEntityIds() throws SQLException {
    return holdingObjectDao.getJoinedItemsIds(holdingObject, property);
  }


  @Override
  public Object get(int index) {
    return items.get(index);
  }

  @Override
  public boolean add(Object object) {
    // TODO: implement Check if object is already persisted (otherwise throw Exception)

    if(object == null) {
//      throw new SQLException("Value to add may not be null");
      return false;
    }

    int oldSize = size();

    add(oldSize, object);

    return oldSize < size();
  }

  @Override
  public void add(int index, Object element) {
    itemAddedToCollection(index, element);
  }

  protected boolean itemAddedToCollection(int index, Object element) {
    items.add(index, element);

    try {
      Object id = targetDao.getObjectId(element);

      if(id == null) { // object not persisted yet
        id = createTemporaryId(element);
      }

      targetEntitiesIds.add(index, id);

      itemAddedToCollection(index, element, id);

      return true;
    } catch(Exception e) {
      log.error("Could not add item " + element + " to Collection of Property " + property, e);
    }

    return false;
  }

  protected void itemAddedToCollection(int index, Object element, Object id) {
    // may be overwritten in subclass
  }

  @Override
  public boolean remove(Object object) {
    boolean success = itemRemovedFromCollection(object);

    return success;
  }

  protected boolean itemRemovedFromCollection(Object object) {
    boolean success = items.remove(object);

    try {
      Object id = targetDao.getObjectId(object);

      if(id == null) {
        id = unpersistedEntities.get(object);
      }

      targetEntitiesIds.remove(id);

      Object unpersistedEntityId = unpersistedEntities.remove(object); // clean up unpersistedEntities
      if(unpersistedEntityId != null) {
        targetEntitiesIds.remove(unpersistedEntityId);
      }

      itemRemovedFromCollection(object, id);
    } catch(Exception e) {
      log.error("Could not remove Entity " + object + " from Property " + property, e);
    }

    return success;
  }

  protected void itemRemovedFromCollection(Object object, Object id) {
    // may be overwritten in subclass
  }


  private Object createTemporaryId(Object element) {
    String temporaryId = UUID.randomUUID().toString();

    unpersistedEntities.put(element, temporaryId);

    return temporaryId;
  }


  @Override
  public int size() {
    return items.size();
  }

  @Override
  public void clear() {
    items.clear();
    targetEntitiesIds.clear();
    unpersistedEntities.clear();
  }

  public void refresh(Collection<Object> targetEntitiesIds) throws SQLException {
    clear();
    retrievedTargetEntities(targetEntitiesIds);
  }


  public List<Object> getTargetEntitiesIds() {
    resolveRealIdsOrPreviouslyUnpersistedEntities();

    return targetEntitiesIds;
  }

  protected void resolveRealIdsOrPreviouslyUnpersistedEntities() {
    for(Object unpersistedEntity : new ArrayList(unpersistedEntities.keySet())) {
      try {
        Object realId = targetDao.getObjectId(unpersistedEntity);

        if(realId != null) { // ok, so now we have the real id, remove the temporary one and set real one
          Object temporaryId = unpersistedEntities.remove(unpersistedEntity);
          int index = targetEntitiesIds.indexOf(temporaryId);
          targetEntitiesIds.set(index, realId);
        }
      } catch(Exception ignored) { }
    }
  }

}

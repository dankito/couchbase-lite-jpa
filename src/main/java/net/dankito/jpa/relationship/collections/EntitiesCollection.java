package net.dankito.jpa.relationship.collections;

import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.couchbaselite.Dao;

import java.sql.SQLException;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 21/08/16.
 */
public class EntitiesCollection extends AbstractList implements Set {

  // we need to put items in a specific order, so i use a List instead of a Set
  protected List items = new CopyOnWriteArrayList();

  protected Object holdingObject; // TODO: find a better name

  protected PropertyConfig property;

  protected Dao holdingObjectDao;

  protected Dao targetDao;


  public EntitiesCollection(Object object, PropertyConfig property, Dao holdingObjectDao, Dao targetDao) throws SQLException {
    this.holdingObject = object;
    this.property = property;
    this.holdingObjectDao = holdingObjectDao;
    this.targetDao = targetDao;

    initializeCollection();
  }


  protected void initializeCollection() throws SQLException {
    Collection<Object> ids = getJoinedEntityIds();

    addAll(targetDao.retrieve(ids));
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
    return true;
  }

  @Override
  public boolean remove(Object object) {
    boolean success = itemRemovedFromCollection(object);

    return success;
  }

  protected boolean itemRemovedFromCollection(Object object) {
    return items.remove(object);
  }


  @Override
  public int size() {
    return items.size();
  }

  @Override
  public void clear() {
    items.clear();
  }

  public void refresh() throws SQLException {
    clear();
    initializeCollection();
  }

}

package net.dankito.jpa.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cannot make this Class generic as Entities in Application may have different type of IDs (e.g. Long and String)
 * and Entities' classes are as well different.
 *
 * Created by ganymed on 18/08/16.
 */
public class ObjectCache {

  protected Map<Class, Map<Object, Object>> cache = new ConcurrentHashMap<>();


  public boolean containsObjectForId(Class entityClass, Object id) {
    return cache.containsKey(entityClass) && cache.get(entityClass).containsKey(id);
  }

  public void add(Class entityClass, Object id, Object object) {
    if(cache.containsKey(entityClass) == false) {
      cache.put(entityClass, new ConcurrentHashMap<>());
    }

    cache.get(entityClass).put(id, object);
  }

  /**
   * Returns null if there's no cached Object for entityClass and id!
   * @param entityClass
   * @param id
   * @return Cached Object or null
   */
  public Object get(Class entityClass, Object id) {
    if(containsObjectForId(entityClass, id)) {
      return cache.get(entityClass).get(id);
    }

    return null;
  }

  public Object remove(Class entityClass, Object id) {
    if(containsObjectForId(entityClass, id)) {
      Object removedObject = cache.get(entityClass).remove(id);

      if(cache.get(entityClass).isEmpty()) {
        cache.remove(entityClass);
      }

      return removedObject;
    }

    return null;
  }

}

package net.dankito.jpa.cache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cannot make this Class generic as Entities in Application may have different type of IDs (e.g. Long and String)
 * and Entities' classes are as well different.
 *
 * Created by ganymed on 18/08/16.
 */
public class ObjectCache {

  protected Map<Class, Map<Object, Object>> cache = new ConcurrentHashMap<>();


  /**
   * Try to avoid calling this method as it's of O(n).<br/>
   * Try to get or remove object directly.
   * @param entityClass
   * @param id
   * @return
   */
  public boolean containsObjectForId(Class entityClass, Object id) {
    return id != null && cache.containsKey(entityClass) && cache.get(entityClass).containsKey(id);
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
    Map<Object, Object> cachedEntityObjects = cache.get(entityClass);
    if(cachedEntityObjects != null) {
      return cachedEntityObjects.get(id);
    }

    return null;
  }

  public Collection<Object> getAllOfClass(Class entityClass) {
    Set<Object> cachedEntitiesOfClass = new HashSet<>();
    Map<Object, Object> cachedEntityObjects = cache.get(entityClass);

    if(cachedEntityObjects != null) {
      cachedEntitiesOfClass.addAll(cachedEntityObjects.values());
    }

    return cachedEntitiesOfClass;
  }

  public Object remove(Class entityClass, Object id) {
    Map<Object, Object> cachedEntityObjects = cache.get(entityClass);

    if(cachedEntityObjects != null) {
      Object removedObject = cachedEntityObjects.remove(id);

      if(cachedEntityObjects.isEmpty()) {
        cache.remove(entityClass);
      }

      return removedObject;
    }

    return null;
  }


  public void clear() {
    cache.clear();
  }

}

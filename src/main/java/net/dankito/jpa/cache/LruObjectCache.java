package net.dankito.jpa.cache;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Caches only the least recently used objects.
 * If there are more objects than countMaxItemsInCache in cache the longest unused objects (that are for those add() or get() hasn't been called for while) will be removed from
 * cache.
 */
public class LruObjectCache extends ObjectCache {

  private static final int DEFAULT_COUNT_MAX_ITEMS_IN_CACHE = Integer.MAX_VALUE;


  private int countMaxItemsInCache;

  final private Set<Object> lastRecentlyUsedObjects = new LinkedHashSet<>();


  public LruObjectCache() {
    this(DEFAULT_COUNT_MAX_ITEMS_IN_CACHE);
  }

  public LruObjectCache(int countMaxItemsInCache) {
    this.countMaxItemsInCache = countMaxItemsInCache;
  }


  @Override
  public void add(Class entityClass, Object id, Object object) {
    super.add(entityClass, id, object);

    objectAddedToCache(id);
  }

  protected void objectAddedToCache(Object id) {
    synchronized(lastRecentlyUsedObjects) {
      lastRecentlyUsedObjects.add(id);

      if(lastRecentlyUsedObjects.size() > countMaxItemsInCache) {
        removeLongestNotUsedItemFromCache();
      }
    }
  }

  protected void removeLongestNotUsedItemFromCache() {
    while(lastRecentlyUsedObjects.size() > countMaxItemsInCache) {
      Iterator<Object> iterator = lastRecentlyUsedObjects.iterator();
      Object objectToRemoveFromCacheId = iterator.next();
      iterator.remove();

      try {
        for(Map<Object, Object> entityCache : cache.values()) {
          if(entityCache.remove(objectToRemoveFromCacheId) != null) {
            break;
          }
        }
      } catch (Exception ignored) { }
    }
  }


  /**
   * Returns null if there's no cached Object for entityClass and id!
   * @param entityClass
   * @param id
   * @return Cached Object or null
   */
  @Override
  public Object get(Class entityClass, Object id) {
    Object cachedObject = super.get(entityClass, id);

    if(cachedObject != null) {
      synchronized (lastRecentlyUsedObjects) {
        lastRecentlyUsedObjects.remove(id);
        lastRecentlyUsedObjects.add(id); // move object to top of queue as it's now the least recently used one
      }
    }

    return cachedObject;
  }


  @Override
  public Object remove(Class entityClass, Object id) {
    synchronized(lastRecentlyUsedObjects) {
      lastRecentlyUsedObjects.remove(id);
    }

    return super.remove(entityClass, id);
  }

  public void clear() {
    super.clear();

    lastRecentlyUsedObjects.clear();
  }

}

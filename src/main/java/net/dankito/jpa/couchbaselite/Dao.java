package net.dankito.jpa.couchbaselite;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.Revision;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.View;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.jpa.annotationreader.config.DataType;
import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.OrderByConfig;
import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.annotationreader.config.inheritance.DiscriminatorColumnConfig;
import net.dankito.jpa.cache.DaoCache;
import net.dankito.jpa.cache.ObjectCache;
import net.dankito.jpa.relationship.collections.EntitiesCollection;
import net.dankito.jpa.relationship.collections.LazyLoadingEntitiesCollection;
import net.dankito.jpa.relationship.collections.LazyLoadingManyToManyEntitiesCollection;
import net.dankito.jpa.relationship.collections.ManyToManyEntitiesCollection;
import net.dankito.jpa.util.CrudOperation;
import net.dankito.jpa.util.DatabaseCompacter;
import net.dankito.jpa.util.IValueConverter;
import net.dankito.jpa.util.ValueConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.AccessType;

/**
 * In contrary to JPA standard a bidirectional @OneToMany additionally stores its joined entity ids also on the one side in a String array.
 * The same does an unidirectional @OneToMany (instead of creating a Join Table according to standard).
 *
 * CascadeType.Merge and CascadeType.Detach are currently completely ignored.
 *
 * Created by ganymed on 15/08/16.
 */
public class Dao {
  private static final Logger log = LoggerFactory.getLogger(Dao.class);

  public static final String ID_SYSTEM_COLUMN_NAME = "_id";

  public static final String REVISION_SYSTEM_COLUMN_NAME = "_rev";

  public static final String REVISIONS_SYSTEM_COLUMN_NAME = "_revisions";

  public static final String DELETED_SYSTEM_COLUMN_NAME = "_deleted";

  public static final String ATTACHMENTS_SYSTEM_COLUMN_NAME = "_attachments";

  public static final String TYPE_COLUMN_NAME = "type_";

  public static final String PARENT_ENTITY_CLASSES_COLUMN_NAME = "parent_entity_classes";

  protected static final int TOO_LARGE_TOO_RETRIEVE_BY_IDS = 1000;

  protected static final int TOO_LARGE_TOO_SORT_MANUALLY = 500;

  protected static final long ATTACHMENT_SIZE_TO_COMPACT_DATABASE_AFTER_REMOVAL = 500 * 1024; // 500 kByte


  protected Database database;

  protected EntityConfig entityConfig;

  protected Class entityClass;

  protected ObjectCache objectCache;

  protected DaoCache daoCache;

  protected DatabaseCompacter databaseCompacter;

  protected IValueConverter valueConverter;

  protected ObjectMapper objectMapper = null;

  protected Map<Class, View> queryForAllEntitiesOfDataTypeViews = new ConcurrentHashMap<>();


  protected boolean retrieveListOfIdsByView = true;

  protected boolean retrieveDocumentsForSortingByView = true;


  public Dao(Database database, EntityConfig entityConfig, ObjectCache objectCache, DaoCache daoCache) {
    this(database, entityConfig, objectCache, daoCache, new DatabaseCompacter(database, 10000));
  }

  public Dao(Database database, EntityConfig entityConfig, ObjectCache objectCache, DaoCache daoCache, DatabaseCompacter databaseCompacter) {
    this(database, entityConfig, objectCache, daoCache, databaseCompacter, new ValueConverter());
  }

  public Dao(Database database, EntityConfig entityConfig, ObjectCache objectCache, DaoCache daoCache, DatabaseCompacter databaseCompacter, IValueConverter valueConverter) {
    this.database = database;
    this.entityConfig = entityConfig;
    this.objectCache = objectCache;
    this.daoCache = daoCache;
    this.databaseCompacter = databaseCompacter;
    this.valueConverter = valueConverter;

    this.entityClass = entityConfig.getEntityClass();
  }


  public boolean create(Object object) throws SQLException, CouchbaseLiteException {
    checkIfCrudOperationCanBePerformedOnObjectOfClass(object, CrudOperation.CREATE);

    entityConfig.invokePrePersistLifeCycleMethod(object);

    createEntityInDb(object);

    entityConfig.invokePostPersistLifeCycleMethod(object);

    return true;
  }

  protected Document createEntityInDb(Object object) throws SQLException, CouchbaseLiteException {
    Document newDocument = createDocumentForObject(object);

    setIdOnObject(object, newDocument);
    addObjectToCache(object, newDocument.getId());

    Map<String, Object> mappedProperties = mapProperties(object, entityConfig, newDocument, true);

    createCascadePersistPropertiesAndUpdateDocument(object, mappedProperties);

    updateDocument(newDocument, mappedProperties);

    updateVersionOnObject(object, newDocument);

    return newDocument;
  }

  protected Document createDocumentForObject(Object object) throws SQLException {
    String objectId = getObjectId(object);

    // TODO: this is wrong: An Object with Id already set should only be persisted if Ids aren't auto generated
    if(objectId != null) { // User has set Id, use that one
      return database.getDocument(objectId);
    }
    else {
      return database.createDocument();
    }
  }

  protected void createCascadePersistPropertiesAndUpdateDocument(Object object, Map<String, Object> mappedProperties) throws SQLException, CouchbaseLiteException {
    Map<String, Object> cascadedProperties = createCascadePersistProperties(object);

    if(cascadedProperties.size() > 0) {
      mappedProperties.putAll(cascadedProperties);
    }
  }

  protected Map<String, Object> createCascadePersistProperties(Object object) throws SQLException, CouchbaseLiteException {
    Map<String, Object> cascadedProperties = new HashMap<>();

    for(PropertyConfig cascadePersistProperty : entityConfig.getRelationshipPropertiesWithCascadePersistIncludingInheritedOnes()) {
      Dao targetDao = daoCache.getTargetDaoForRelationshipProperty(cascadePersistProperty);
      Object propertyValue = getPropertyValue(object, cascadePersistProperty);

      if(propertyValue != null) {
        if(cascadePersistProperty.isCollectionProperty()) {
          if(cascadePersistProperty.isManyToManyField() == false) {
            persistOneToManyCollectionItems(cascadePersistProperty, targetDao, cascadedProperties, (Collection) propertyValue);
          }
          else {
            persistManyToManyCollectionItems(cascadePersistProperty, targetDao, cascadedProperties, (Collection) propertyValue);
          }

          if(propertyValue instanceof EntitiesCollection == false) {
            createAndSetEntitiesCollectionAndAddExistingItems(object, cascadePersistProperty, propertyValue);
          }
        }
        else {
          if(isAlreadyPersisted(propertyValue) == false) {
            targetDao.create(propertyValue); // don't check return value of create() as if entity has been created in the meantime it would return false and its id therefore won't get persisted
          }
          cascadedProperties.put(cascadePersistProperty.getColumnName(), targetDao.getObjectId(propertyValue));
        }
      }
    }

    return cascadedProperties;
  }

  protected void persistOneToManyCollectionItems(PropertyConfig cascadePersistProperty, Dao targetDao, Map<String, Object> documentProperties, Collection propertyValue) throws CouchbaseLiteException, SQLException {
    List<String> persistedItemIds = new ArrayList<>();

    for(Object item : propertyValue) {
      if(isAlreadyPersisted(item) == false) {
        targetDao.create(item);
      }

      persistedItemIds.add(targetDao.getObjectId(item));
    }

    writeOneToManyJoinedEntityIdsToProperty(persistedItemIds, cascadePersistProperty, documentProperties);
  }

  protected void persistManyToManyCollectionItems(PropertyConfig cascadePersistProperty, Dao targetDao, Map<String, Object> cascadedProperties, Collection propertyValue) throws CouchbaseLiteException, SQLException {
    // TODO: currently we storing redundantly target entities' ids on both side, may implement JoinTables for ManyToMany properties
    persistOneToManyCollectionItems(cascadePersistProperty, targetDao, cascadedProperties, propertyValue);
  }


  public Collection<Object> retrieve(final Collection<Object> ids) throws SQLException {
    Collection<Object> retrievedObjects = new ArrayList<>();

    if(ids.size() > 0) {
      if(retrieveListOfIdsByView) {
        retrieveListOfIdsByView(ids, retrievedObjects);
      }
      else {
        for(Object id : ids) {
          retrievedObjects.add(retrieve(id));
        }
      }
    }

    return retrievedObjects;
  }

  protected void retrieveListOfIdsByView(Collection<Object> ids, Collection<Object> retrievedObjects) throws SQLException {
    Date startTime = new Date();

    View queryForAllEntitiesOfDataTypeView = getOrCreateQueryForAllEntitiesOfDataTypeView(entityClass);
    Query query = queryForAllEntitiesOfDataTypeView.createQuery();

    try {
      if(isTooLargeToRetrieveByIds(ids)) {
        retrieveListOfIdsByViewForLargerCollection(ids, retrievedObjects, query);
      }
      else {
        retrieveListOfIdsByViewForSmallerCollection(ids, retrievedObjects, query);
      }
    } catch (Exception e) {
      log.error("Could not query for list of ids", e);
      throw new SQLException("Could not query for list of ids", e);
    }

    logOperationDurationDuration("Retrieving " + ids.size() + " of Type " + entityClass.getSimpleName(), startTime);
  }

  /**
   * At a certain amount of Ids using Query.setKeys() results in that generated SQL Statement by Couchbase Lite becomes to large
   * @param ids
   * @return
   */
  protected boolean isTooLargeToRetrieveByIds(Collection<Object> ids) {
    return ids.size() > TOO_LARGE_TOO_RETRIEVE_BY_IDS;
  }

  protected void retrieveListOfIdsByViewForLargerCollection(Collection<Object> ids, Collection<Object> retrievedObjects, Query query) throws CouchbaseLiteException, SQLException {
    QueryEnumerator enumerator = query.run();

    boolean queriedWholeCollection = enumerator.getCount() == ids.size(); // only check if result item's id is in ids if not all entities have been queried
    List<Object> idsCopy = null;
    if(queriedWholeCollection == false) {
      idsCopy = new ArrayList<>(ids);
    }

    while(enumerator.hasNext()) {
      QueryRow nextResultItem = enumerator.next();
      Object objectId = nextResultItem.getDocumentId();

      if(queriedWholeCollection || idsCopy.remove(objectId)) {
        addToRetrievedObjects(retrievedObjects, nextResultItem, objectId);
      }
    }
  }

  protected void retrieveListOfIdsByViewForSmallerCollection(Collection<Object> ids, Collection<Object> retrievedObjects, Query query) throws CouchbaseLiteException, SQLException {
    if (ids instanceof List) {
      query.setKeys((List<Object>) ids);
    }
    else {
      query.setKeys(new ArrayList<Object>(ids));
    }

    QueryEnumerator enumerator = query.run();

    while (enumerator.hasNext()) {
      QueryRow nextResultItem = enumerator.next();
      Object objectId = nextResultItem.getDocumentId();

      addToRetrievedObjects(retrievedObjects, nextResultItem, objectId);
    }
  }

  protected void addToRetrievedObjects(Collection<Object> retrievedObjects, QueryRow nextResultItem, Object objectId) throws SQLException {
    Object cachedOrRetrievedObject = objectCache.get(entityClass, objectId);
    if(cachedOrRetrievedObject == null) {
      cachedOrRetrievedObject = createObjectFromDocument(nextResultItem.getDocument(), objectId);
      objectCache.add(entityClass, objectId, cachedOrRetrievedObject);
    }

    if(cachedOrRetrievedObject != null) { // TODO: why checking for != null before adding to retrievedObjects?
      retrievedObjects.add(cachedOrRetrievedObject);
    }
  }

  protected void logOperationDurationDuration(String operationName, Date startTime) {
    long millisecondsElapsed = (new Date().getTime() - startTime.getTime());
    if(millisecondsElapsed > 10) {
      log.info(operationName + " took " + createTimeElapsedString(millisecondsElapsed) + " seconds");
    }
  }

  public String createTimeElapsedString(long millisecondsElapsed) {
    return (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3);
  }

  public Object retrieve(Object id) throws SQLException {
    Object cachedObject = getObjectFromCache(id); // don't check first if cache contains object to this id as this is operation is of O(n)
    if(cachedObject != null) {
      return cachedObject;
    }
    else {
      return retrieveObjectFromDb(id);
    }
  }

  protected Object retrieveObjectFromDb(Object id) throws SQLException {
    Document storedDocument = retrieveStoredDocumentForId(id);

    return createObjectFromDocument(storedDocument, id);
  }

  protected Object createObjectFromDocument(Document storedDocument, Object id) throws SQLException {
    Class entityRealClass = getEntityClassFromDocument(storedDocument);

    return createObjectFromDocument(storedDocument, id, entityRealClass);
  }

  protected Object createObjectFromDocument(Document storedDocument, Object id, Class entityRealClass) throws SQLException {
    if(entityConfig.getEntityClass().equals(entityRealClass)) {
      Object retrievedObject = createObjectInstance(id);

      setPropertiesOnObject(retrievedObject, storedDocument);

      entityConfig.invokePostLoadLifeCycleMethod(retrievedObject);

      return retrievedObject;
    }
    else { // for classes with inheritance may for a parent class is queried, but we need to create an instance of child class
      if(containsParentEntityClass(storedDocument, entityClass) == false) {
        throw new SQLException("Trying to retrieve an Object of Type " + entityClass + " of ID " + id + ", but Document with this ID says it's of Type " + entityRealClass + " " +
            "which is not a child class of " + entityClass);
      }

      Dao childDao = daoCache.getDaoForEntity(entityRealClass);
      return childDao.createObjectFromDocument(storedDocument, id, entityRealClass);
    }
  }


  public <T> List<T> retrieveAllEntitiesOfType(Class<T> entityType) {
    View queryForAllEntitiesOfDataTypeView = getOrCreateQueryForAllEntitiesOfDataTypeView(entityType);

    return queryForAllEntitiesOfDataType(queryForAllEntitiesOfDataTypeView);
  }

  protected <T> View getOrCreateQueryForAllEntitiesOfDataTypeView(Class<T> entityType) {
    View queryForAllEntitiesOfDataTypeView = queryForAllEntitiesOfDataTypeViews.get(entityType);
    if(queryForAllEntitiesOfDataTypeView == null) {
      queryForAllEntitiesOfDataTypeView = createQueryForAllEntitiesOfDataTypeView(entityType);
      queryForAllEntitiesOfDataTypeViews.put(entityType, queryForAllEntitiesOfDataTypeView);
    }
    return queryForAllEntitiesOfDataTypeView;
  }

  protected  <T> View createQueryForAllEntitiesOfDataTypeView(Class<T> type) {
    final String fullTypeName = type.getName();
    View queryForAllEntitiesOfDataTypeView;
    synchronized(this) {
      queryForAllEntitiesOfDataTypeView = database.getView(fullTypeName);
    }

    queryForAllEntitiesOfDataTypeView.setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if(fullTypeName.equals(document.get(Dao.TYPE_COLUMN_NAME))) {
          emitter.emit(document.get(ID_SYSTEM_COLUMN_NAME), null);
        }
      }
    }, "1.0");

    return queryForAllEntitiesOfDataTypeView;
  }

  protected <T> List<T> queryForAllEntitiesOfDataType(View queryForAllEntitiesOfDataTypeView) {
    List<T> queryResult = new ArrayList<T>();

    try {
      QueryEnumerator enumerator = queryForAllEntitiesOfDataTypeView.createQuery().run();
      while(enumerator.hasNext()) {
        QueryRow nextResultItem = enumerator.next();
        Class entityClass = getEntityClassFromDocument(nextResultItem.getDocument());

        T cachedEntity = (T)objectCache.get(entityClass, nextResultItem.getDocumentId());
        if(cachedEntity != null) {
          queryResult.add(cachedEntity);
        }
        else {
          T retrievedEntity = (T)createObjectFromDocument(nextResultItem.getDocument(), nextResultItem.getDocumentId(), entityClass);
          if(retrievedEntity != null) {
            queryResult.add(retrievedEntity);
          }
        }
      }
    } catch(Exception e) {
      log.error("Could not query for all Entities of Data Type " + entityClass, e);
      // TODO: throw SQLException?
    }

    return queryResult;
  }


  protected Object createObjectInstance(Object id) throws SQLException {
    try {
      Object newInstance = entityConfig.getConstructor().newInstance();

      addObjectToCache(newInstance, id);

      return newInstance;
    } catch(Exception e) {
      throw new SQLException("Could not create Instance of " + entityConfig.getEntityClass().getSimpleName(), e);
    }
  }

  protected void setPropertiesOnObject(Object object, Document document) throws SQLException {
    if(getObjectId(object) == null) { // on Entities with Inheritance ID might already be (correctly) set -> don't overwrite it
      setIdOnObject(object, document);
    }
    updateVersionOnObject(object, document);

    for(PropertyConfig property : entityConfig.getPropertiesIncludingInheritedOnes()) {
      if(isCouchbaseLiteSystemProperty(property) == false && property instanceof DiscriminatorColumnConfig == false) {
        try {
          setPropertyOnObject(object, document, property); // TODO: catch Exception for setting single Property or let it bubble up and therefor stop Object creation?
        } catch(Exception e) {
          log.error("Could not set property " + property + " on Object " + object, e);
        }
      }
    }
  }

  protected void setPropertyOnObject(Object object, Document document, PropertyConfig property) throws SQLException {
    if(property.isLob()) {
      Object propertyValue = getLobFromAttachment(property, document);
      setValueOnObject(object, property, propertyValue);
    }
    else {
      Object propertyValueFromDocument = getValueFromDocument(document, property);

      setPropertyOnObjectToValueFromDocument(object, document, property, propertyValueFromDocument);
    }
  }

  protected void setPropertyOnObjectToValueFromDocument(Object object, Document document, PropertyConfig property, Object propertyValueFromDocument) throws SQLException {
    if(propertyValueFromDocument == null) {
      if(document.getProperties().containsKey(property.getColumnName())) { // only if null value is explicitly set in Document (if it doesn't contain key property.getColumnName() also null is returned)
        setValueOnObject(object, property, propertyValueFromDocument);
      }
    }
    else if(property.isRelationshipProperty() == false) {
      setValueOnObject(object, property, propertyValueFromDocument);
    }
    else {
      Dao targetDao = daoCache.getTargetDaoForRelationshipProperty(property);

      if(property.isCollectionProperty() == false) {
        Object deserializedTargetInstance = targetDao.retrieve(propertyValueFromDocument);
        setValueOnObject(object, property, deserializedTargetInstance);
      }
      else {
        setCollectionPropertyOnObject(object, property, targetDao, (String)propertyValueFromDocument);
      }
    }
  }

  protected void setCollectionPropertyOnObject(Object object, PropertyConfig property, Dao targetDao, String joinedEntityIdsString) throws SQLException {
    Object propertyValue = getPropertyValue(object, property);

    Collection<Object> targetEntitiesIds = targetDao.parseAndSortJoinedEntityIdsFromJsonString(object, joinedEntityIdsString, property);

    if(propertyValue instanceof EntitiesCollection == false) {
      createAndSetEntitiesCollection(object, property, targetEntitiesIds);
    }
    else {
      ((EntitiesCollection)propertyValue).refresh(targetEntitiesIds);
    }
  }

  public Object deserializePersistedValue(Object object, PropertyConfig property, Object propertyValueFromDocument) throws SQLException {
    Object deserializedPropertyValue = convertPersistedValue(propertyValueFromDocument, property);

    if(property.isRelationshipProperty() && propertyValueFromDocument != null) {
      Dao targetDao = daoCache.getTargetDaoForRelationshipProperty(property);

      if(property.isCollectionProperty() == false) {
        deserializedPropertyValue = targetDao.retrieve(propertyValueFromDocument);
      }
      else {
        Collection<Object> itemIds = targetDao.parseAndSortJoinedEntityIdsFromJsonString(object, (String)propertyValueFromDocument, property);

        deserializedPropertyValue = targetDao.retrieve(itemIds);
      }
    }

    return deserializedPropertyValue;
  }

  protected Object getValueFromDocument(Document document, PropertyConfig property) {
    Object value = document.getProperty(property.getColumnName());

    return convertPersistedValue(value, property);
  }

  protected Object convertPersistedValue(Object value, PropertyConfig property) {
    return valueConverter.convertRetrievedValue(property, value);
  }


  public boolean update(Object object) throws SQLException, CouchbaseLiteException {
    checkIfCrudOperationCanBePerformedOnObjectOfClass(object, CrudOperation.UPDATE);

    updateEntityInDb(object);

    return true;
  }

  protected void updateEntityInDb(Object object) throws SQLException, CouchbaseLiteException {
    Document storedDocument = retrieveStoredDocument(object);

    entityConfig.invokePreUpdateLifeCycleMethod(object);

    final Map<String, Object> updatedProperties = mapProperties(object, entityConfig, storedDocument, false);

    updateDocument(storedDocument, updatedProperties);

    updateVersionOnObject(object, storedDocument);

    // TODO: is there a kind of Cascade Update?

    entityConfig.invokePostUpdateLifeCycleMethod(object);
  }

  protected void updateDocument(Document storedDocument, final Map<String, Object> updatedProperties) {
    // see http://blog.couchbase.com/2016/july/better-updates-couchbase-lite
    try {
      storedDocument.update(new Document.DocumentUpdater() {
        @Override
        public boolean update(UnsavedRevision newRevision) {
          Map<String, Object> properties = newRevision.getUserProperties();
          properties.putAll(updatedProperties);
          newRevision.setUserProperties(properties);
          return true;
        }
      });
    } catch(Exception e) {
      log.error("Could not update Document with Id " + storedDocument.getId() + " to Properties: " + updatedProperties);
    }
  }


  protected Document retrieveStoredDocument(Object object) throws SQLException {
    String id = getObjectId(object);

    return retrieveStoredDocumentForId(id);
  }

  protected Document retrieveStoredDocumentForId(Object id) throws SQLException {
    Document storedDocument = database.getExistingDocument((String)id);

    if(storedDocument == null) {
      throw new SQLException("There's no existing Document with ID " + id + " for Type " + entityConfig.getEntityName());
    }

    return storedDocument;
  }

  protected Class getEntityClassFromDocument(Document document) throws SQLException {
    String className = (String) document.getProperty(TYPE_COLUMN_NAME);
    try {
      return Class.forName(className);
    } catch(Exception e) {
      throw new SQLException("Could not find Class for " + className, e);
    }
  }


  public boolean delete(Object object) throws SQLException, CouchbaseLiteException {
    checkIfCrudOperationCanBePerformedOnObjectOfClass(object, CrudOperation.DELETE);

    Document storedDocument = retrieveStoredDocument(object);
    boolean result = false;

    if(storedDocument.isDeleted() == false) {
      entityConfig.invokePreRemoveLifeCycleMethod(object);
      String id = storedDocument.getId();

      result = deleteObjectInDb(object, storedDocument);

      // TODO: should id be reset on Object?

      removeObjectFromCache(id);

      deleteCascadeRemoveProperties(object);

      entityConfig.invokePostRemoveLifeCycleMethod(object);
    }

    return result;
  }

  protected boolean deleteObjectInDb(Object object, Document storedDocument) throws CouchbaseLiteException, SQLException {
    boolean result = storedDocument.delete();

    if(result) {
      setValueOnObject(object, entityConfig.getIdProperty(), null); // TODO: really set Id to null?
      updateVersionOnObject(object, storedDocument); // TODO: after delete documents version is set to null -> really update object's version?
    }

    return result;
  }


  protected void deleteCascadeRemoveProperties(Object object) throws SQLException, CouchbaseLiteException {
    for(PropertyConfig cascadeRemoveProperty : entityConfig.getRelationshipPropertiesWithCascadeRemoveIncludingInheritedOnes()) {
      Dao targetDao = daoCache.getTargetDaoForRelationshipProperty(cascadeRemoveProperty);
      Object propertyValue = getPropertyValue(object, cascadeRemoveProperty);

      if(propertyValue != null) { // TODO: check if propertyValue's ID is set (if null means already deleted)?
        if(cascadeRemoveProperty.isCollectionProperty()) {
          for(Object item : (Collection)propertyValue) {
            if(isAlreadyPersisted(item)) {
              if(targetDao.delete(item)) {
                // TODO: remove item from Collection then?
              }
            }
          }
        }
        else {
          if(isAlreadyPersisted(propertyValue)) {
            if(targetDao.delete(propertyValue)) {
              // TODO: set Property value to null then?
            }
          }
        }
      }
    }
  }


  public Collection<Object> getJoinedItemsIds(Object object, PropertyConfig collectionProperty) throws SQLException {
    if(isAlreadyPersisted(object)) {
      Document objectDocument = retrieveStoredDocument(object);

      String itemIdsString = (String) objectDocument.getProperties().get(collectionProperty.getColumnName());

      if (itemIdsString != null) { // on initial EntitiesCollection creation itemIdsString is null
        return parseAndSortJoinedEntityIdsFromJsonString(object, itemIdsString, collectionProperty);
      }
    }

    return new HashSet<>();
  }

  protected Collection<Object> parseAndSortJoinedEntityIdsFromJsonString(Object object, String itemIdsString, PropertyConfig property) throws SQLException {
    Collection<Object> itemIds = parseJoinedEntityIdsFromJsonString(itemIdsString);

    if(property.hasOrderColumns()) {
      itemIds = sortItems(object, itemIds, property);
    }

    return itemIds;
  }

  public Collection<Object> parseJoinedEntityIdsFromJsonString(String itemIdsString) throws SQLException {
    try {
      ObjectMapper objectMapper = getObjectMapper();

      return objectMapper.readValue(itemIdsString, Set.class);
    } catch (Exception e) {
      throw new SQLException("Could not parse String " + itemIdsString + " to List with joined Entity Ids", e);
    }
  }

  /**
   * For Couchbase Lite we have to implement sorting Entities according @OrderBy Annotations in memory.
   *
   * @param object
   * @param itemIds
   * @param property
   * @return
   */
  protected Collection<Object> sortItems(Object object, final Collection<Object> itemIds, final PropertyConfig property) throws SQLException {
    if(itemIds.size() < 2) { // less then 2 item -> nothing to sort
      return itemIds;
    }

    // there's not other way to sort ids then loading their Documents which may thwart Lazy Loading
    Collection<Object> sortedIds = new ArrayList<>();
    Date startTime = new Date();

    if(isTooLargeToSortManually(itemIds)) {
      sortedIds = sortItemsByView(object, itemIds, property);
    }
    else {
      sortedIds = sortItemsByManualComparison(itemIds, property);
    }

    logOperationDurationDuration("Sorting " + itemIds.size() + " items", startTime);

    if(sortedIds.size() != itemIds.size()) { // fallback is sortedIds doesn't contain all itemIds
      sortedIds = itemIds;
    }

    return sortedIds;
  }

  /**
   * <p>
   *  Usually it's best to create as few Views as possible, because they all have to be
   *  created (which takes a long time for large data sets) and kept up to date.
   * </p>
   * <p>
   *  But when the data set grows too large, manual sorting just takes to much time.
   *  In these cases creating a View is better for overall performance, even so that
   *  it means that for each Entity with a @OrderBy property with a large data set an
   *  extra View has to be created and kept up to date.
   * </p>
   * @param ids
   * @return
   */
  protected boolean isTooLargeToSortManually(Collection<Object> ids) {
    return ids.size() > TOO_LARGE_TOO_SORT_MANUALLY;
  }

  protected Collection<Object> sortItemsByView(Object object, Collection<Object> itemIds, PropertyConfig property) {
    List<Object> sortedIds = new ArrayList(); // do not use a HashSet as it ruins ordering
    Date startTime = new Date();

    try {
      View queryForPropertyWithOrderByView = createViewForPropertyWithOrderBy(object, itemIds, property);
      Query query = queryForPropertyWithOrderByView.createQuery();

      // TODO: currently only for the first @OrderBy column ascending or descending can be specified
      if(property.hasOrderColumns()) {
        query.setDescending(! property.getOrderColumns().get(0).isAscending());
      }

      QueryEnumerator enumerator = query.run();
      final List<Object> itemIdsCopy = new ArrayList<>(itemIds);

      while(enumerator.hasNext()) {
        QueryRow nextResultItem = enumerator.next();
        String id = nextResultItem.getDocumentId();

        if(itemIdsCopy.remove(id)) {
          sortedIds.add(nextResultItem.getDocumentId());
        }
      }
    } catch (Exception e) {
      log.error("Could sort Entities of " + property + " by it @OrderBy columns", e);
//      throw new SQLException("Could sort Entities of " + property + " by it @OrderBy columns", e); // TODO: throw SQLException?
      sortedIds = new ArrayList<>(itemIds);
    }

    logOperationDurationDuration("View Sorting Documents of " + property + " by " + property.getOrderColumns().get(0).getColumnName(), startTime);

    return sortedIds;
  }

  protected View createViewForPropertyWithOrderBy(Object object, Collection<Object> itemIds, final PropertyConfig property) throws SQLException {
    View viewForPropertyWithOrderBy;
    synchronized(this) {
      viewForPropertyWithOrderBy = database.getView(property.getEntityConfig().getEntityName() + "_" + property.getColumnName());
    }

    viewForPropertyWithOrderBy.setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        List<Object> sortKeys = new ArrayList<>();

        for(OrderByConfig orderBy : property.getOrderColumns()) {
          sortKeys.add(document.get(orderBy.getColumnName()));
        }

        emitter.emit(sortKeys, null);
      }
    }, "1.0");

    return viewForPropertyWithOrderBy;
  }

  protected Collection<Object> sortItemsByManualComparison(Collection<Object> itemIds, PropertyConfig property) throws SQLException {
    List<Object> sortedIds = new ArrayList(itemIds); // do not use a HashSet as it ruins ordering
    Date startTime = new Date();

    final Map<Object, Document> mapIdToDocument = getDocumentsToIds(itemIds);
    logOperationDurationDuration("Getting Documents for sorting", startTime);
    startTime = new Date();

    for (int i = property.getOrderColumns().size() - 1; i >= 0; i--) {
      final OrderByConfig orderBy = property.getOrderColumns().get(i);

      // do not use a TreeSet as when Comparator returns 0, one of the two objects gets removed from TreeSet
      Collections.sort(sortedIds, new Comparator<Object>() {
        @Override
        public int compare(Object id1, Object id2) {
          return compareObjects(mapIdToDocument, id1, id2, orderBy);
        }
      });
    }

    logOperationDurationDuration("Manual Sorting Documents of " + property + " by " + property.getOrderColumns().get(0).getColumnName(), startTime);

    return sortedIds;
  }

  protected Map<Object, Document> getDocumentsToIds(Collection<Object> itemIds) throws SQLException {
    Map<Object, Document> mapIdToDocument = new HashMap<>();

    if(retrieveDocumentsForSortingByView) {
      mapIdToDocument = getDocumentsToIdsByView(itemIds);
    }
    else {
      for (Object id : itemIds) {
        mapIdToDocument.put(id, retrieveStoredDocumentForId(id));
      }
    }


    return mapIdToDocument;
  }

  protected Map<Object, Document> getDocumentsToIdsByView(Collection<Object> itemIds) {
    Map<Object, Document> mapIdToDocument = new HashMap<>();

    View view = getOrCreateQueryForAllEntitiesOfDataTypeView(entityClass);
    Query query = view.createQuery();

    try {
      if(isTooLargeToRetrieveByIds(itemIds)) {
        getDocumentsToIdsByViewForLargerCollections(itemIds, mapIdToDocument, query);
      }
      else {
        getDocumentsToIdsByViewForSmallerCollections(itemIds, mapIdToDocument, query);
      }
    } catch (Exception e) {
      log.error("Could not get documents for ids by view", e);
//      throw new SQLException("Could not get documents for ids by view", e); // TODO: throw SQLException?
    }

    return mapIdToDocument;
  }

  protected void getDocumentsToIdsByViewForLargerCollections(Collection<Object> itemIds, Map<Object, Document> mapIdToDocument, Query query) throws CouchbaseLiteException {
    QueryEnumerator enumerator = query.run();

    boolean queriedWholeCollection = enumerator.getCount() == itemIds.size(); // only check if result item's id is in itemIds if not all entities have been queried
    List<Object> itemIdsCopy = null;
    if(queriedWholeCollection == false) {
      itemIdsCopy = new ArrayList<>(itemIds);
    }

    while(enumerator.hasNext()) {
      QueryRow nextResultItem = enumerator.next();
      Object id = nextResultItem.getDocumentId();

      if(queriedWholeCollection || itemIdsCopy.remove(id)) {
        mapIdToDocument.put(id, nextResultItem.getDocument());
      }
    }
  }

  protected void getDocumentsToIdsByViewForSmallerCollections(Collection<Object> itemIds, Map<Object, Document> mapIdToDocument, Query query) throws CouchbaseLiteException {
    if (itemIds instanceof List) {
      query.setKeys((List<Object>) itemIds);
    }
    else {
      query.setKeys(new ArrayList<Object>(itemIds));
    }

    QueryEnumerator enumerator = query.run();

    while (enumerator.hasNext()) {
      QueryRow nextResultItem = enumerator.next();
      mapIdToDocument.put(nextResultItem.getDocumentId(), nextResultItem.getDocument());
    }
  }

  protected int compareObjects(Map<Object, Document> mapIdToDocument, Object id1, Object id2, OrderByConfig orderBy) {
    Object object1Value = null, object2Value = null;
    try { object1Value = getValueFromDocument(mapIdToDocument.get(id1), orderBy.getOrderByTargetProperty()); } catch(Exception ignored) { }
    try { object2Value = getValueFromDocument(mapIdToDocument.get(id2), orderBy.getOrderByTargetProperty()); } catch(Exception ignored) { }

    if(object1Value == null && object2Value == null) {
      return 0;
    }
    else if(object1Value == null) {
      return -1;
    }
    else if(object2Value == null) {
      return 1;
    }

    int compareValue = 0;

    if(object1Value instanceof Comparable && object2Value instanceof Comparable) {
      compareValue = ((Comparable)object1Value).compareTo(object2Value);

      if(orderBy.isAscending() == false) {
        compareValue = compareValue * (-1); // invert ordering
      }
    }

    return compareValue;
  }


  protected boolean isAlreadyPersisted(Object object) throws SQLException {
    return getObjectId(object) != null;
  }

  public String getObjectId(Object object) throws SQLException {
    Object idValue = getPropertyValue(object, entityConfig.getIdProperty());

    if(idValue instanceof Long) { // TODO: remove
      idValue = ((Long)idValue).toString();
    }
    return (String)idValue;
  }

  protected void setIdOnObject(Object object, Document newDocument) throws SQLException {
    setValueOnObject(object, entityConfig.getIdProperty(), newDocument.getId());
  }

  protected void updateVersionOnObject(Object object, Document document) throws SQLException {
    if(entityConfig.isVersionPropertySet()) {
      if(document.isDeleted() == false) {
        Object version = getDocumentVersion(document);
        setValueOnObject(object, entityConfig.getVersionProperty(), version);
      }
      else { // TODO: what to do when document is deleted? set version to null?
        setValueOnObject(object, entityConfig.getVersionProperty(), null);
      }
    }
  }

  protected Object getDocumentVersion(Document document) throws SQLException {
    String revisionId = document.getCurrentRevisionId();
    revisionId = revisionId.substring(0, revisionId.indexOf('-')); // Version and Revision UUID are separated by a '-'

    Object version = Long.parseLong(revisionId);

    Class versionDataType = entityConfig.getVersionProperty().getType();
    if(int.class.equals(versionDataType) || Integer.class.equals(versionDataType)) {
      version = (int)version;
    }
    else if(short.class.equals(versionDataType) || Short.class.equals(versionDataType)) {
      version = (short)version;
    }
    else if(java.sql.Timestamp.class.equals(versionDataType)) {
      version = new java.sql.Timestamp(new Date().getTime());
    }

    return version;
  }


  protected void checkIfCrudOperationCanBePerformedOnObjectOfClass(Object object, CrudOperation crudOperation) throws SQLException {
    if(object == null) {
      throw new SQLException("Object to " + crudOperation + " may not be null");
    }
    if(entityConfig.getEntityClass().isAssignableFrom(object.getClass()) == false) {
      throw new SQLException("Object to " + crudOperation + " of class " + object.getClass() + " is not of Dao's Entity class " + entityConfig.getEntityClass());
    }

    // TODO: also check again: crudOperation == CrudOperation.CREATE && isAlreadyPersisted(object) == true
    // this is only allowed if selfGeneratedId is set to true -> adjust initialSyncManager accordingly
    if(crudOperation != CrudOperation.CREATE && isAlreadyPersisted(object) == false) {
      throw new SQLException("Object " + object + " is not persisted yet, cannot perform " + crudOperation + ".");
    }
  }


  protected Map<String, Object> mapProperties(Object object, EntityConfig entityConfig, Document document, boolean isInitialPersist) throws SQLException {
    Map<String, Object> mappedProperties = new HashMap<>();

    if(isInitialPersist) { // persist Entity's type on initial persist
      mappedProperties.put(TYPE_COLUMN_NAME, entityConfig.getEntityClass().getName());
      if(entityConfig.hasParentEntityConfig()) {
        writeParentEntityClasses(mappedProperties);
      }
    }

    for(PropertyConfig property : entityConfig.getPropertiesIncludingInheritedOnes()) {
      if(shouldPropertyBeAdded(isInitialPersist, property)) {
        mapProperty(object, mappedProperties, property, isInitialPersist);
      }

      if(property.isLob()) {
        addLobAsAttachment(object, property, document); // TODO: this is actually a side effect, creates a new revision instead of mapping a property
      }
    }

    return mappedProperties;
  }

  protected void mapProperty(Object object, Map<String, Object> mappedProperties, PropertyConfig property, boolean isInitialPersist) throws SQLException {
    Object propertyValue = getPropertyValue(object, property);

    if(property.isRelationshipProperty() == false || propertyValue == null) {
      mappedProperties.put(property.getColumnName(), propertyValue);
    }
    else { // for Objects persist only its ID respectively their IDs for Collections
      if(daoCache.containsTargetDaoForRelationshipProperty(property)) { // on correctly configured Entities should actually never be false
        Dao targetDao = daoCache.getTargetDaoForRelationshipProperty(property);
        if(property.isCollectionProperty() == false) {
          mappedProperties.put(property.getColumnName(), targetDao.getObjectId(propertyValue));
        }
        else {
          mapCollectionProperty(object, property, mappedProperties, targetDao, (Collection)propertyValue, isInitialPersist);
        }
      }
    }
  }

  protected void mapCollectionProperty(Object object, PropertyConfig collectionProperty, Map<String, Object> mappedProperties, Dao targetDao, Collection propertyValue, boolean isInitialPersist) throws SQLException {
    boolean isInitialPersistAndCascadePersistProperty = isInitialPersist && collectionProperty.cascadePersist();

    // otherwise EntitiesCollection will then be created and itemIds be written in createCascadePersistProperties()
    if(isInitialPersistAndCascadePersistProperty == false) {
      if(propertyValue instanceof EntitiesCollection == false) {
        propertyValue = createAndSetEntitiesCollectionAndAddExistingItems(object, collectionProperty, propertyValue);
      }

      List joinedEntityIds = ((EntitiesCollection)propertyValue).getTargetEntitiesIds();

      writeOneToManyJoinedEntityIdsToProperty(joinedEntityIds, collectionProperty, mappedProperties);
    }
  }

  protected EntitiesCollection createEntitiesCollection(Object object, PropertyConfig collectionProperty, Collection<Object> targetEntitiesIds) throws SQLException {
    Dao targetDao = daoCache.getTargetDaoForRelationshipProperty(collectionProperty);
    EntitiesCollection collection = null;

    if(collectionProperty.isManyToManyField() == false) {
      if(collectionProperty.isLazyLoading()) {
        collection = new LazyLoadingEntitiesCollection(object, collectionProperty, this, targetDao, targetEntitiesIds);
      }
      else {
        collection = new EntitiesCollection(object, collectionProperty, this, targetDao, targetEntitiesIds);
      }
    }
    else {
      if(collectionProperty.isLazyLoading()) {
        collection = new LazyLoadingManyToManyEntitiesCollection(object, collectionProperty, this, targetDao, targetEntitiesIds);
      }
      else {
        collection = new ManyToManyEntitiesCollection(object, collectionProperty, this, targetDao, targetEntitiesIds); // TODO: also pass JoinTable Dao
      }
    }

    return collection;
  }

  protected void createAndSetEntitiesCollection(Object object, PropertyConfig collectionProperty, Collection<Object> targetEntitiesIds) throws SQLException {
    Collection collection = createEntitiesCollection(object, collectionProperty, targetEntitiesIds);

    setValueOnObject(object, collectionProperty, collection);
  }

  protected EntitiesCollection createAndSetEntitiesCollectionAndAddExistingItems(Object object, PropertyConfig collectionProperty, Object propertyValue) throws SQLException {
    EntitiesCollection collection = createEntitiesCollection(object, collectionProperty, new ArrayList<Object>());

    for(Object currentItem : (Collection)propertyValue) {
      collection.add(currentItem);
    }

    setValueOnObject(object, collectionProperty, collection);

    return collection;
  }

  protected void writeOneToManyJoinedEntityIdsToProperty(List joinedEntityIds, PropertyConfig property, Map<String, Object> documentProperties) throws SQLException {
    String itemIdsString = getPersistableCollectionTargetEntities(joinedEntityIds);
    documentProperties.put(property.getColumnName(), itemIdsString);
  }


  protected void writeParentEntityClasses(Map<String, Object> mappedProperties) throws SQLException {
    try {
      ObjectMapper objectMapper = getObjectMapper();
      String serializedParentEntityClasses = objectMapper.writeValueAsString(entityConfig.getParentEntityClasses());
      mappedProperties.put(PARENT_ENTITY_CLASSES_COLUMN_NAME, serializedParentEntityClasses);
    } catch (JsonProcessingException e) {
      throw new SQLException("Could not persist Parent Entity Classes", e);
    }
  }

  protected boolean containsParentEntityClass(Document storedDocument, Class parentClass) {
    String parentClassInfo = (String)storedDocument.getProperty(PARENT_ENTITY_CLASSES_COLUMN_NAME);

    return parentClassInfo != null && parentClassInfo.contains(parentClass.getName());
  }


  protected void addLobAsAttachment(Object object, PropertyConfig property, Document document) {
    try {
      byte[] bytes = getContentForAttachment(object, property, document);

      if(bytes == null) { // property value has now ben set to null but wasn't before -> remove attachment
        removeAttachment(property, document);
      }
      else {
        SavedRevision currentRevision = document.getCurrentRevision();
        String attachmentName = getAttachmentNameForProperty(property);
        Attachment previousAttachment = currentRevision == null ? null : currentRevision.getAttachment(attachmentName);
        if(previousAttachment != null && previousAttachment.getLength() == bytes.length) { // should be the same content, don't update
          return;
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        // TODO: don't create a new revision for each attachment (and another one on each update!).
        // Create a new revision and use this for all attachments and mapped properties
        UnsavedRevision newRevision = currentRevision != null ? currentRevision.createRevision() : document.createRevision();
        newRevision.setAttachment(attachmentName, "application/octet-stream", inputStream);

        newRevision.save();
        inputStream.close();
      }
    } catch(Exception e) { log.error("Could not add Lob as Attachment for Property " + property, e); }
  }

  protected byte[] getContentForAttachment(Object object, PropertyConfig property, Document document) throws Exception {
    byte[] bytes = null;
    Object propertyValue = getPropertyValue(object, property);

    if(propertyValue instanceof byte[]) {
      bytes = (byte[])propertyValue;
    }
    else if(propertyValue instanceof String) {
      bytes = ((String)propertyValue).getBytes();
    }
    else {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
      objectOutputStream.writeObject(propertyValue);
      objectOutputStream.close();

      bytes = byteArrayOutputStream.toByteArray(); // TODO: there must be a better way then loading all bytes to memory first
      byteArrayOutputStream.close();
    }

    return bytes;
  }

  protected boolean removeAttachment(PropertyConfig property, Document document) {
    SavedRevision currentRevision = document.getCurrentRevision();
    if(currentRevision != null) {
      Attachment previousValueAttachment = currentRevision.getAttachment(getAttachmentNameForProperty(property));
      if(previousValueAttachment != null) {
        long attachmentSize = previousValueAttachment.getLength();

        if(removeAttachment(property, currentRevision)) {
          if(shouldCompactDatabase(attachmentSize)) {
            compactDatabase();
          }
          return true;
        }
      }
    }

    return false;
  }

  protected boolean removeAttachment(PropertyConfig property, SavedRevision currentRevision) {
    try {
      UnsavedRevision newRevision = currentRevision.createRevision();
      newRevision.removeAttachment(getAttachmentNameForProperty(property));

      newRevision.save();
      return true;
    } catch(Exception e) { log.error("Could not remove attachment for Property " + property, e); }

    return false;
  }

  public Object getLobFromAttachment(PropertyConfig property, Document document) {
    Revision revision = document.getCurrentRevision();
    Attachment attachment = revision.getAttachment(getAttachmentNameForProperty(property));
    try {
      if(attachment != null) {
        return readAttachmentContent(property, attachment);
      }
    } catch(Exception e) { log.error("Could not read Lob from Attachment for Property " + property, e); }

    return null;
  }

  protected Object readAttachmentContent(PropertyConfig property, Attachment attachment) throws CouchbaseLiteException, IOException {
    InputStream inputStream = attachment.getContent();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[16384];

    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();

    byte[] bytes = buffer.toByteArray();

    if(property.getDataType() == DataType.STRING) {
      return new String(bytes);
    }
    else {
      return bytes;
    }
  }

  protected String getAttachmentNameForProperty(PropertyConfig property) {
    return property.getColumnName();
  }


  public boolean shouldCompactDatabase(long attachmentSize) {
    return attachmentSize > ATTACHMENT_SIZE_TO_COMPACT_DATABASE_AFTER_REMOVAL;
  }

  public void compactDatabase() {
    if(databaseCompacter != null) {
      databaseCompacter.scheduleCompacting();
    }
  }


  protected boolean shouldPropertyBeAdded(boolean isInitialPersist, PropertyConfig property) {
    return isCouchbaseLiteSystemProperty(property) == false && property.isLob() == false;
  }

  protected boolean isCouchbaseLiteSystemProperty(PropertyConfig property) {
    return property.isId() || property.isVersion();
  }


  public Object getPersistablePropertyValue(Object object, PropertyConfig property) throws SQLException {
    Object persistablePropertyValue = getPropertyValue(object, property);

    if(property.isRelationshipProperty() && persistablePropertyValue != null) { // for Objects persist only its ID respectively their IDs for Collections
      if(daoCache.containsTargetDaoForRelationshipProperty(property)) { // on correctly configured Entities should actually never be false
        Dao targetDao = daoCache.getTargetDaoForRelationshipProperty(property);

        if(property.isCollectionProperty() == false) {
          persistablePropertyValue = targetDao.getObjectId(persistablePropertyValue);
        }
        else {
          persistablePropertyValue = getPersistableCollectionPropertyValue(targetDao, (Collection)persistablePropertyValue);
        }
      }
    }

    return persistablePropertyValue;
  }

  protected Object getPersistableCollectionPropertyValue(Dao targetDao, Collection propertyValue) throws SQLException {
    List joinedEntityIds = new ArrayList();

    if(propertyValue instanceof EntitiesCollection) {
      joinedEntityIds = ((EntitiesCollection)propertyValue).getTargetEntitiesIds();
    }
    else { // TODO: will it ever come to this?
      for(Object item : propertyValue) {
        Object itemId = targetDao.getObjectId(item);
        if(itemId != null) { // TODO: what if item is not persisted yet? // should actually never be the case as not persisted entities may not get added to EntitiesCollection
          joinedEntityIds.add(itemId);
        }
      }
    }

    return getPersistableCollectionTargetEntities(joinedEntityIds);
  }

  public String getPersistableCollectionTargetEntities(Collection joinedEntityIds) throws SQLException {
    try {
      ObjectMapper objectMapper = getObjectMapper();
      return objectMapper.writeValueAsString(joinedEntityIds);
    } catch (JsonProcessingException e) {
      throw new SQLException("Could not persist IDs of Collection Items", e);
    }
  }

  protected Object getPropertyValue(Object object, PropertyConfig property) throws SQLException {
    Object value;

    if(property instanceof DiscriminatorColumnConfig) {
      DiscriminatorColumnConfig discriminatorColumn = (DiscriminatorColumnConfig)property;
      value = discriminatorColumn.getDiscriminatorValue(object);
    }
    else {
      value = extractValueFromObject(object, property);
    }

    value = valueConverter.convertValueForPersistence(property, value);

    return value;
  }

  public Object extractValueFromObject(Object object, PropertyConfig property) throws SQLException {
    Object value;

    if(shouldUseGetter(property)) {
      try {
        value = property.getFieldGetMethod().invoke(object);
      } catch (Exception e) {
        throw new SQLException("Could not call " + property.getFieldGetMethod() + " for " + property, e);
      }
    }
    else {
      try {
        value = property.getField().get(object);
      } catch (Exception e) {
        throw new SQLException("Could not get field value for " + property, e);
      }
    }

    return value;
  }


  public void setValueOnObject(Object object, PropertyConfig property, Object value) throws SQLException {
    if(shouldUseSetter(property)) {
      try {
        property.getFieldSetMethod().invoke(object, value);
      } catch (Exception e) {
        throw new SQLException("Could not call " + property.getFieldSetMethod() + " for " + property, e);
      }
    }
    else {
      try {
        // field object may not be a T yet
        property.getField().set(object, value);
      } catch (Exception e) {
        throw new SQLException("Could not set field value for " + property, e);
      }
    }
  }

  protected boolean shouldUseGetter(PropertyConfig property) {
    return (entityConfig.getAccess() == AccessType.PROPERTY && property.getFieldGetMethod() != null) ||
        (property.getField() == null && property.getFieldGetMethod() != null);
  }

  protected boolean shouldUseSetter(PropertyConfig property) {
    return (entityConfig.getAccess() == AccessType.PROPERTY && property.getFieldSetMethod() != null) ||
        (property.getField() == null && property.getFieldSetMethod() != null);
  }



  protected Object getObjectFromCache(Object id) {
    return objectCache.get(entityClass, id);
  }

  protected void addObjectToCache(Object object, Object id) {
    objectCache.add(entityClass, id, object);
  }

  protected void removeObjectFromCache(String id) {
    objectCache.remove(entityClass, id);
  }


  protected ObjectMapper getObjectMapper() {
    if(objectMapper == null) {
      objectMapper = new ObjectMapper();
    }

    return objectMapper;
  }


  public EntityConfig getEntityConfig() {
    return entityConfig;
  }

}

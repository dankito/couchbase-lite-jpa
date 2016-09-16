package net.dankito.jpa.couchbaselite;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import net.dankito.jpa.util.IValueConverter;
import net.dankito.jpa.util.ValueConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public static final String ID_COLUMN_NAME = "_id";

  public static final String TYPE_COLUMN_NAME = "type_";

  public static final String PARENT_ENTITY_CLASSES_COLUMN_NAME = "parent_entity_classes";


  protected Database database;

  protected EntityConfig entityConfig;

  protected Class entityClass;

  protected ObjectCache objectCache;

  protected DaoCache daoCache;

  protected IValueConverter valueConverter;

  protected ObjectMapper objectMapper = null;

  protected Map<Class, View> queryForAllEntitiesOfDataTypeViews = new ConcurrentHashMap<>();


  protected boolean retrieveListOfIdsByView = true;

  protected boolean retrieveDocumentsForSortingByView = true;


  public Dao(Database database, EntityConfig entityConfig, ObjectCache objectCache, DaoCache daoCache) {
    this(database, entityConfig, objectCache, daoCache, new ValueConverter());
  }

  public Dao(Database database, EntityConfig entityConfig, ObjectCache objectCache, DaoCache daoCache, IValueConverter valueConverter) {
    this.database = database;
    this.entityConfig = entityConfig;
    this.objectCache = objectCache;
    this.daoCache = daoCache;
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

    Map<String, Object> mappedProperties = mapProperties(object, entityConfig, null);

    newDocument.putProperties(mappedProperties);

    setIdOnObject(object, newDocument);
    updateVersionOnObject(object, newDocument);

    addObjectToCache(object, newDocument.getId());

    createCascadePersistPropertiesAndUpdateDocument(object, newDocument);

    return newDocument;
  }

  protected Document createDocumentForObject(Object object) throws SQLException {
    String objectId = getObjectId(object);
    if(objectId != null) { // User has set Id, use that one
      return database.getDocument(objectId);
    }
    else {
      return database.createDocument();
    }
  }

  protected void createCascadePersistPropertiesAndUpdateDocument(Object object, Document objectDocument) throws SQLException, CouchbaseLiteException {
    Map<String, Object> cascadedProperties = createCascadePersistProperties(object);

    if(cascadedProperties.size() > 0) {
      Map<String, Object> documentProperties = new HashMap<>();
      documentProperties.putAll(objectDocument.getProperties());
      documentProperties.putAll(cascadedProperties);

      objectDocument.putProperties(documentProperties); // TODO: this is bad as in this way in one create() two Revisions get created (but we need to persist object first prior to cascading its Properties)
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
    if (ids instanceof List) {
      query.setKeys((List<Object>) ids);
    }
    else {
      query.setKeys(new ArrayList<Object>(ids));
    }

    try {
      QueryEnumerator enumerator = query.run();
      while (enumerator.hasNext()) {
        QueryRow nextResultItem = enumerator.next();
        String objectId = nextResultItem.getDocumentId();

        Object cachedOrRetrievedObject = objectCache.get(entityClass, objectId);
        if(cachedOrRetrievedObject == null) {
          cachedOrRetrievedObject = createObjectFromDocument(nextResultItem.getDocument(), objectId);
          objectCache.add(entityClass, nextResultItem.getDocumentId(), cachedOrRetrievedObject);
        }

        if(cachedOrRetrievedObject != null) { // TODO: why checking for != null before adding to retrievedObjects?
          retrievedObjects.add(cachedOrRetrievedObject);
        }
      }
    } catch (Exception e) {
      log.error("Could not query for list of ids", e);
      throw new SQLException("Could not query for list of ids", e);
    }

    logOperationDurationDuration("Retrieving " + ids.size() + " of Type " + entityClass.getSimpleName(), startTime);
  }

  protected void logOperationDurationDuration(String operationName, Date startTime) {
    long millisecondsElapsed = (new Date().getTime() - startTime.getTime());
    if(millisecondsElapsed > 2) {
      log.info(operationName + " took " + createTimeElapsedString(millisecondsElapsed) + " seconds");
    }
  }

  public String createTimeElapsedString(long millisecondsElapsed) {
    return (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3);
  }

  public Object retrieve(Object id) throws SQLException {
    // TODO: don't call contains as this is of O(n), get it directly and check for != null
    if(isObjectCachedForId(id)) {
      return getObjectFromCache(id);
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
      return childDao.createObjectFromDocument(storedDocument, id);
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
    View queryForAllEntitiesOfDataTypeView = database.getView(fullTypeName);

    queryForAllEntitiesOfDataTypeView.setMap(new Mapper() {
      @Override
      public void map(Map<String, Object> document, Emitter emitter) {
        if(fullTypeName.equals(document.get(Dao.TYPE_COLUMN_NAME))) {
          emitter.emit(document.get(ID_COLUMN_NAME), null);
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
        T retrievedEntity = (T)createObjectFromDocument(nextResultItem.getDocument(), nextResultItem.getDocumentId());
        if(retrievedEntity != null) {
          queryResult.add(retrievedEntity);
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
        setPropertyOnObject(object, document, property); // TODO: catch Exception for setting single Property or let it bubble up and therefor stop Object creation?
      }
    }
  }

  protected void setPropertyOnObject(Object object, Document document, PropertyConfig property) throws SQLException {
    Object propertyValueFromDocument = getValueFromDocument(document, property);

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

    Collection<Object> targetEntitiesIds = targetDao.parseAndSortJoinedEntityIdsFromJsonString(joinedEntityIdsString, property);

    if(propertyValue instanceof EntitiesCollection == false) {
      createAndSetEntitiesCollection(object, property, targetEntitiesIds);
    }
    else {
      ((EntitiesCollection)propertyValue).refresh(targetEntitiesIds);
    }
  }

  public Object deserializePersistedValue(PropertyConfig property, Object propertyValueFromDocument) throws SQLException {
    Object deserializedPropertyValue = convertPersistedValue(propertyValueFromDocument, property);

    if(property.isRelationshipProperty() && propertyValueFromDocument != null) {
      Dao targetDao = daoCache.getTargetDaoForRelationshipProperty(property);

      if(property.isCollectionProperty() == false) {
        deserializedPropertyValue = targetDao.retrieve(propertyValueFromDocument);
      }
      else {
        Collection<Object> itemIds = targetDao.parseAndSortJoinedEntityIdsFromJsonString((String)propertyValueFromDocument, property);

        deserializedPropertyValue = targetDao.retrieve(itemIds);
      }
    }

    return deserializedPropertyValue;
  }

  protected Collection<Object> deserializeCollectionPropertyValue(PropertyConfig property, Dao targetDao, String joinedEntityIdsString) throws SQLException {
    Collection<Object> itemIds = targetDao.parseAndSortJoinedEntityIdsFromJsonString(joinedEntityIdsString, property);

    return targetDao.retrieve(itemIds);
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

    Map<String, Object> updatedProperties = mapProperties(object, entityConfig, storedDocument);

    storedDocument.putProperties(updatedProperties);

    updateVersionOnObject(object, storedDocument);

    // TODO: is there a kind of Cascade Update?

    entityConfig.invokePostUpdateLifeCycleMethod(object);
  }


  protected Document retrieveStoredDocument(Object object) throws SQLException {
    String id = getObjectId(object);

    return retrieveStoredDocumentForId(id);
  }

  protected Document retrieveStoredDocumentForId(Object id) throws SQLException {
    Document storedDocument = database.getExistingDocument((String)id);

    if(storedDocument == null) {
      throw new SQLException("There's no existing Document with ID " + id);
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
        return parseAndSortJoinedEntityIdsFromJsonString(itemIdsString, collectionProperty);
      }
    }

    return new HashSet<>();
  }

  protected Collection<Object> parseAndSortJoinedEntityIdsFromJsonString(String itemIdsString, PropertyConfig property) throws SQLException {
    Collection<Object> itemIds = parseJoinedEntityIdsFromJsonString(itemIdsString);

    if(property.hasOrderColumns()) {
      itemIds = sortItems(itemIds, property);
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
   * @param itemIds
   * @param property
   * @return
   */
  protected Collection<Object> sortItems(final Collection<Object> itemIds, final PropertyConfig property) throws SQLException {
    // there's not other way to sort ids then loading their Documents which may thwart Lazy Loading
    Collection<Object> sortedIds = new ArrayList<>();
    Date startTime = new Date();

    sortedIds = sortItemsByManualComparison(itemIds, property);

    logOperationDurationDuration("Sorting " + itemIds.size() + " items", startTime);

    if(sortedIds.size() != itemIds.size()) { // fallback is sortedIds doesn't contain all itemIds
      sortedIds = itemIds;
    }

    return sortedIds;
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

    logOperationDurationDuration("Sorting Documents of " + property + " by " + property.getOrderColumns().get(0), startTime);

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

    if (itemIds instanceof List) {
      query.setKeys((List<Object>) itemIds);
    }
    else {
      query.setKeys(new ArrayList<Object>(itemIds));
    }

    try {
      QueryEnumerator enumerator = query.run();

      while (enumerator.hasNext()) {
        QueryRow nextResultItem = enumerator.next();
        mapIdToDocument.put(nextResultItem.getDocumentId(), nextResultItem.getDocument());
      }
    } catch (Exception e) {
      log.error("Could not get documents for ids by view", e);
//      throw new SQLException("Could not get documents for ids by view", e); // TODO: throw SQLException?
    }

    return mapIdToDocument;
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

    if(idValue instanceof Long) {
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

    if(crudOperation != CrudOperation.CREATE && isAlreadyPersisted(object) == false) {
      throw new SQLException("Object " + object + " is not persisted yet, cannot perform " + crudOperation + ".");
    }
  }


  protected Map<String, Object> mapProperties(Object object, EntityConfig entityConfig, Document storedDocument) throws SQLException {
    Map<String, Object> mappedProperties = new HashMap<>();

    boolean isInitialPersist = storedDocument == null;
    if(storedDocument != null) {
      mappedProperties.putAll(storedDocument.getProperties());
    }
    else { // persist Entity's type on initial persist
      mappedProperties.put(TYPE_COLUMN_NAME, entityConfig.getEntityClass().getName());
      if(entityConfig.hasParentEntityConfig()) {
        writeParentEntityClasses(mappedProperties);
      }
    }

    for(PropertyConfig property : entityConfig.getPropertiesIncludingInheritedOnes()) {
      if(shouldPropertyBeAdded(isInitialPersist, property)) {
        mapProperty(object, mappedProperties, property, isInitialPersist);
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


  protected boolean shouldPropertyBeAdded(boolean isInitialPersist, PropertyConfig property) {
    return isCouchbaseLiteSystemProperty(property) == false;
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



  protected boolean isObjectCachedForId(Object id) {
    return objectCache.containsObjectForId(entityClass, id);
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

package net.dankito.jpa.couchbaselite;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;

import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.annotationreader.config.PropertyConfig;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.AccessType;

/**
 * Created by ganymed on 15/08/16.
 */
public class Dao {

  protected Database database;

  protected EntityConfig entityConfig;


  public Dao(Database database, EntityConfig entityConfig) {
    this.database = database;
    this.entityConfig = entityConfig;
  }


  public boolean persist(Object object) throws SQLException, CouchbaseLiteException {
    if(object == null) {
      throw new SQLException("Object to persist may not be null");
    }
    if(entityConfig.getEntityClass().isAssignableFrom(object.getClass()) == false) {
      throw new SQLException("Object to persist of class " + object.getClass() + " is not of Dao's Entity class " + entityConfig.getEntityClass());
    }

    entityConfig.invokePrePersistLifeCycleMethod(object);

    Document newDocument = database.createDocument();

    Map<String, Object> mappedProperties = mapProperties(object, entityConfig, true);

    newDocument.putProperties(mappedProperties);

    setValueOnObject(object, entityConfig.getIdProperty(), newDocument.getId());
    if(entityConfig.isVersionPropertySet()) {
      setValueOnObject(object, entityConfig.getVersionProperty(), newDocument.getCurrentRevisionId());
    }

    entityConfig.invokePostPersistLifeCycleMethod(object);

    return false;
  }


  protected Map<String, Object> mapProperties(Object object, EntityConfig entityConfig, boolean isForInitialPersist) throws SQLException {
    Map<String, Object> mappedProperties = new HashMap<>();

    for(PropertyConfig property : entityConfig.getPropertyConfigs()) {
      if(shouldPropertyBeAdded(isForInitialPersist, property)) {
        Object propertyValue = readPropertyValue(object, property);
        mappedProperties.put(property.getColumnName(), propertyValue);
      }
    }

    return mappedProperties;
  }

  protected boolean shouldPropertyBeAdded(boolean isForInitialPersist, PropertyConfig property) {
    return ! (isForInitialPersist && (property.isId() || property.isVersion()) );
  }

  protected Object readPropertyValue(Object object, PropertyConfig property) throws SQLException {
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


  protected void setValueOnObject(Object object, PropertyConfig property, Object value) throws SQLException {
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

}

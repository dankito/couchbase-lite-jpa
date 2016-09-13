package net.dankito.jpa.relationship.collections;

import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.couchbaselite.Dao;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Created by ganymed on 22/08/16.
 */
public class ManyToManyEntitiesCollection extends EntitiesCollection {

  public ManyToManyEntitiesCollection(Object object, PropertyConfig property, Dao holdingObjectDao, Dao targetDao, Collection<Object> targetEntitiesIds) throws SQLException {
    super(object, property, holdingObjectDao, targetDao, targetEntitiesIds);
  }


  @Override
  protected Collection<Object> getJoinedEntityIds() throws SQLException {
    // Usually we would have to query a JoinTable to get joined Entity IDs, but currently these are directly stored in Property's value
    return super.getJoinedEntityIds();
  }

}

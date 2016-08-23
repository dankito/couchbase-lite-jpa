package net.dankito.jpa.couchbaselite.relationship;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.couchbaselite.DaoTestBase;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalEagerManySideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalEagerOneSideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalManySideEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToManyBidirectionalOneSideEntity;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by ganymed on 18/08/16.
 */
public class OneToManyBidirectionalEagerRelationshipDaoTest extends OneToManyBidirectionalRelationshipDaoTestBase {


  @Override
  protected Class[] getEntitiesToTest() {
    return new Class[] { OneToManyBidirectionalEagerOneSideEntity.class, OneToManyBidirectionalEagerManySideEntity.class };
  }


  protected OneToManyBidirectionalOneSideEntity createTestOneSideEntity(Collection<OneToManyBidirectionalManySideEntity> manySides) {
    List<OneToManyBidirectionalEagerManySideEntity> castedManySides = new ArrayList<>();

    if(manySides != null) {
      for (OneToManyBidirectionalManySideEntity manySide : manySides) {
        castedManySides.add((OneToManyBidirectionalEagerManySideEntity) manySide);
      }
    }

    return new OneToManyBidirectionalEagerOneSideEntity(castedManySides);
  }

  protected OneToManyBidirectionalManySideEntity createTestManySideEntity(int order) {
    return new OneToManyBidirectionalEagerManySideEntity(order);
  }

  protected Dao getManySideDao() {
    return relationshipDaoCache.getDaoForEntity(OneToManyBidirectionalEagerManySideEntity.class);
  }

}

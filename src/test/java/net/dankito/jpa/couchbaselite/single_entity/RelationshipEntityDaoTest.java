package net.dankito.jpa.couchbaselite.single_entity;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;

import net.dankito.jpa.annotationreader.JpaEntityConfigurationReader;
import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.couchbaselite.DaoTestBase;
import net.dankito.jpa.couchbaselite.testmodel.EntityWithAllDataTypes;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToOneInverseEntity;
import net.dankito.jpa.couchbaselite.testmodel.relationship.OneToOneOwningEntity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

/**
 * Created by ganymed on 18/08/16.
 */
public class RelationshipEntityDaoTest extends DaoTestBase {


  @Override
  protected Class[] getEntitiesToTest() {
    return new Class[] { OneToOneOwningEntity.class, OneToOneInverseEntity.class };
  }


  @Test
  public void oneToOne_AllPropertiesGetPersistedCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    Document persistedOwningSideDocument = database.getDocument(owningSide.getId());
    Assert.assertNotNull(persistedOwningSideDocument);

    Assert.assertEquals(inverseSide.getId(), persistedOwningSideDocument.getProperty(OneToOneOwningEntity.INVERSE_SIDE_COLUMN_NAME + "_id"));

    Document persistedInverseSideDocument = database.getDocument(inverseSide.getId());
    Assert.assertNotNull(persistedInverseSideDocument);

    Assert.assertEquals(owningSide.getId(), persistedInverseSideDocument.getProperty(OneToOneInverseEntity.OWNING_SIDE_COLUMN_NAME + "_id"));
  }

  @Test
  public void oneToOne_InfrastructurePropertiesGetSetCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    Assert.assertNotNull(owningSide.getId());
    Assert.assertNotNull(owningSide.getVersion());
    Assert.assertNotNull(owningSide.getCreatedOn());
    Assert.assertNotNull(owningSide.getModifiedOn());
    Assert.assertEquals(owningSide.getCreatedOn(), owningSide.getModifiedOn());

    Assert.assertNotNull(inverseSide.getId());
    Assert.assertNotNull(inverseSide.getVersion());
    Assert.assertNotNull(inverseSide.getCreatedOn());
    Assert.assertNotNull(inverseSide.getModifiedOn());
    Assert.assertEquals(inverseSide.getCreatedOn(), inverseSide.getModifiedOn());
  }

  @Test
  public void oneToOne_LifeCycleMethodsGetCalledCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneInverseEntity inverseSide = new OneToOneInverseEntity();
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(inverseSide);

    underTest.create(owningSide);

    Assert.assertTrue(owningSide.hasPrePersistBeenCalled());
    Assert.assertTrue(owningSide.hasPostPersistBeenCalled());
    Assert.assertFalse(owningSide.hasPostLoadBeenCalled());
    Assert.assertFalse(owningSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(owningSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(owningSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(owningSide.hasPostRemoveBeenCalled());

    Assert.assertTrue(inverseSide.hasPrePersistBeenCalled());
    Assert.assertTrue(inverseSide.hasPostPersistBeenCalled());
    Assert.assertFalse(inverseSide.hasPostLoadBeenCalled());
    Assert.assertFalse(inverseSide.hasPreUpdateBeenCalled());
    Assert.assertFalse(inverseSide.hasPostUpdateBeenCalled());
    Assert.assertFalse(inverseSide.hasPreRemoveBeenCalled());
    Assert.assertFalse(inverseSide.hasPostRemoveBeenCalled());
  }


  @Test
  public void oneToOne_InverseSideIsNull_NoExceptionsAndAllPropertiesGetPersistedCorrectly() throws CouchbaseLiteException, SQLException {
    OneToOneOwningEntity owningSide = new OneToOneOwningEntity(null);

    underTest.create(owningSide);

    Document persistedOwningSideDocument = database.getDocument(owningSide.getId());
    Assert.assertNotNull(persistedOwningSideDocument);

    Assert.assertEquals(null, persistedOwningSideDocument.getProperty(OneToOneOwningEntity.INVERSE_SIDE_COLUMN_NAME + "_id"));
  }

}

package net.dankito.jpa.couchbaselite.unreleated;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.Document;

import net.dankito.jpa.couchbaselite.DaoTestBase;
import net.dankito.jpa.couchbaselite.testmodel.EntityWithLobs;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class LobTest extends DaoTestBase {

  protected static final String TEST_CLOB = "Mahatma Gandhi";
  protected static final String TEST_CLOB_AFTER_UPDATE = "Mother Teresa";

  protected static final byte[] TEST_BLOB = "Mahatma Gandhi".getBytes();
  protected static final byte[] TEST_BLOB_AFTER_UPDATE = "Mother Teresa".getBytes();


  @Override
  protected Class[] getEntitiesToTest() {
    return new Class[] { EntityWithLobs.class };
  }


  @Test
  public void persistBlob() throws Exception {
    EntityWithLobs testEntity = new EntityWithLobs();
    testEntity.setBlob(TEST_BLOB);


    underTest.create(testEntity);


    Document persistedDocument = database.getDocument(testEntity.getId());
    Assert.assertNotNull(persistedDocument);

    Attachment blobAttachment = persistedDocument.getCurrentRevision().getAttachment(EntityWithLobs.BLOB_COLUMN_NAME);
    Assert.assertNotNull(blobAttachment);
    Assert.assertTrue(blobAttachment.getLength() > 0);
    Assert.assertNotNull(blobAttachment.getContent());

    byte[] blob = readDataFromInputStream(blobAttachment.getContent());
    Assert.assertArrayEquals(TEST_BLOB, blob);
  }

  @Test
  public void updateBlob() throws Exception {
    EntityWithLobs testEntity = new EntityWithLobs();
    testEntity.setBlob(TEST_BLOB);

    underTest.create(testEntity);


    testEntity.setBlob(TEST_BLOB_AFTER_UPDATE);

    underTest.update(testEntity);


    Document persistedDocument = database.getDocument(testEntity.getId());
    Assert.assertNotNull(persistedDocument);

    Attachment blobAttachment = persistedDocument.getCurrentRevision().getAttachment(EntityWithLobs.BLOB_COLUMN_NAME);
    Assert.assertNotNull(blobAttachment);
    Assert.assertTrue(blobAttachment.getLength() > 0);
    Assert.assertNotNull(blobAttachment.getContent());

    byte[] blob = readDataFromInputStream(blobAttachment.getContent());
    Assert.assertArrayEquals(TEST_BLOB_AFTER_UPDATE, blob);
  }

  @Test
  public void removeBlob() throws Exception {
    EntityWithLobs testEntity = new EntityWithLobs();
    testEntity.setBlob(TEST_BLOB);

    underTest.create(testEntity);


    testEntity.setBlob(null);

    underTest.update(testEntity);


    Document persistedDocument = database.getDocument(testEntity.getId());
    Assert.assertNotNull(persistedDocument);

    Attachment blobAttachment = persistedDocument.getCurrentRevision().getAttachment(EntityWithLobs.BLOB_COLUMN_NAME);
    Assert.assertNull(blobAttachment);
  }


  @Test
  public void persistClob() throws Exception {
    EntityWithLobs testEntity = new EntityWithLobs();
    testEntity.setClob(TEST_CLOB);


    underTest.create(testEntity);


    Document persistedDocument = database.getDocument(testEntity.getId());
    Assert.assertNotNull(persistedDocument);

    Attachment clobAttachment = persistedDocument.getCurrentRevision().getAttachment(EntityWithLobs.CLOB_COLUMN_NAME);
    Assert.assertNotNull(clobAttachment);
    Assert.assertTrue(clobAttachment.getLength() > 0);
    Assert.assertNotNull(clobAttachment.getContent());

    byte[] clob = readDataFromInputStream(clobAttachment.getContent());
    String decodedClob = new String(clob);
    Assert.assertEquals(TEST_CLOB, decodedClob);
  }

  @Test
  public void updateClob() throws Exception {
    EntityWithLobs testEntity = new EntityWithLobs();
    testEntity.setClob(TEST_CLOB);

    underTest.create(testEntity);


    testEntity.setClob(TEST_CLOB_AFTER_UPDATE);

    underTest.update(testEntity);


    Document persistedDocument = database.getDocument(testEntity.getId());
    Assert.assertNotNull(persistedDocument);

    Attachment clobAttachment = persistedDocument.getCurrentRevision().getAttachment(EntityWithLobs.CLOB_COLUMN_NAME);
    Assert.assertNotNull(clobAttachment);
    Assert.assertTrue(clobAttachment.getLength() > 0);
    Assert.assertNotNull(clobAttachment.getContent());

    byte[] clob = readDataFromInputStream(clobAttachment.getContent());
    String decodedClob = new String(clob);
    Assert.assertEquals(TEST_CLOB_AFTER_UPDATE, decodedClob);
  }

  @Test
  public void removeClob() throws Exception {
    EntityWithLobs testEntity = new EntityWithLobs();
    testEntity.setClob(TEST_CLOB);

    underTest.create(testEntity);


    testEntity.setClob(null);

    underTest.update(testEntity);


    Document persistedDocument = database.getDocument(testEntity.getId());
    Assert.assertNotNull(persistedDocument);

    Attachment clobAttachment = persistedDocument.getCurrentRevision().getAttachment(EntityWithLobs.CLOB_COLUMN_NAME);
    Assert.assertNull(clobAttachment);
  }


  @Test
  public void retrieveEntityWithBlob() throws Exception {
    EntityWithLobs testEntity = new EntityWithLobs();
    testEntity.setBlob(TEST_BLOB);

    underTest.create(testEntity);

    objectCache.clear();


    EntityWithLobs retrievedEntity = (EntityWithLobs)underTest.retrieve(testEntity.getId());


    Assert.assertNotNull(retrievedEntity);
    Assert.assertNotNull(retrievedEntity.getBlob());
    Assert.assertArrayEquals(TEST_BLOB, retrievedEntity.getBlob());
  }

  @Test
  public void retrieveEntityWithClob() throws Exception {
    EntityWithLobs testEntity = new EntityWithLobs();
    testEntity.setClob(TEST_CLOB);

    underTest.create(testEntity);

    objectCache.clear();


    EntityWithLobs retrievedEntity = (EntityWithLobs)underTest.retrieve(testEntity.getId());


    Assert.assertNotNull(retrievedEntity);
    Assert.assertNotNull(retrievedEntity.getClob());
    Assert.assertEquals(TEST_CLOB, retrievedEntity.getClob());
  }


  protected byte[] readDataFromInputStream(InputStream inputStream) throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[16384];

    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();

    return buffer.toByteArray();
  }

}

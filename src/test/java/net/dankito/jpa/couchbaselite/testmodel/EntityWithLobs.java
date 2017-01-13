package net.dankito.jpa.couchbaselite.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;


@Entity
public class EntityWithLobs extends BaseEntity {

  public static final String CLOB_COLUMN_NAME = "clob";

  public static final String BLOB_COLUMN_NAME = "blob";


  @Lob
  @Column(name = CLOB_COLUMN_NAME)
  protected String clob;

  @Lob
  @Column(name = BLOB_COLUMN_NAME)
  protected byte[] blob;


  public String getClob() {
    return clob;
  }

  public void setClob(String clob) {
    this.clob = clob;
  }

  public byte[] getBlob() {
    return blob;
  }

  public void setBlob(byte[] blob) {
    this.blob = blob;
  }

}

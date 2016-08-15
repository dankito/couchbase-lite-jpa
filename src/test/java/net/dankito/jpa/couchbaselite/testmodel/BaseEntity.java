package net.dankito.jpa.couchbaselite.testmodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

/**
 * Created by ganymed on 15/08/16.
 */
@MappedSuperclass
public class BaseEntity {

  @Id
  protected String id;

  @Version
  protected String version;

  @Column(name = "created_on", updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date createdOn;

  @Column(name = "modified_on")
  @Temporal(TemporalType.TIMESTAMP)
  protected Date modifiedOn;



  @PrePersist
  protected void prePersist() {
    createdOn = new Date();
    modifiedOn = createdOn;
  }

  @PreUpdate
  protected void preUpdate() {
    modifiedOn = new Date();
  }

  @PreRemove
  protected void preRemove() {
    modifiedOn = new Date();
  }


  public String getId() {
    return id;
  }

  public String getVersion() {
    return version;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public Date getModifiedOn() {
    return modifiedOn;
  }
}

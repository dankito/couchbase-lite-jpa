package net.dankito.jpa.couchbaselite.testmodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
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


  protected boolean hasPrePersistBeenCalled = false;
  protected boolean hasPostPersistBeenCalled = false;

  protected boolean hasPostLoadBeenCalled = false;

  protected boolean hasPreUpdateBeenCalled = false;
  protected boolean hasPostUpdateBeenCalled = false;

  protected boolean hasPreRemoveBeenCalled = false;
  protected boolean hasPostRemoveBeenCalled = false;



  @PrePersist
  protected void prePersist() {
    createdOn = new Date();
    modifiedOn = createdOn;

    hasPrePersistBeenCalled = true;
  }

  @PostPersist
  protected void postPersist() {
    hasPostPersistBeenCalled = true;
  }

  @PostLoad
  protected void postLoad() {
    hasPostLoadBeenCalled = true;
  }

  @PreUpdate
  protected void preUpdate() {
    modifiedOn = new Date();

    hasPreUpdateBeenCalled = true;
  }

  @PostUpdate
  protected void postUpdate() {
    hasPostUpdateBeenCalled = true;
  }

  @PreRemove
  protected void preRemove() {
    modifiedOn = new Date();

    hasPreRemoveBeenCalled = true;
  }

  @PostRemove
  protected void postRemove() {
    hasPostRemoveBeenCalled = true;
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


  public boolean hasPrePersistBeenCalled() {
    return hasPrePersistBeenCalled;
  }

  public boolean hasPostPersistBeenCalled() {
    return hasPostPersistBeenCalled;
  }

  public boolean hasPostLoadBeenCalled() {
    return hasPostLoadBeenCalled;
  }

  public boolean hasPreUpdateBeenCalled() {
    return hasPreUpdateBeenCalled;
  }

  public boolean hasPostUpdateBeenCalled() {
    return hasPostUpdateBeenCalled;
  }

  public boolean hasPreRemoveBeenCalled() {
    return hasPreRemoveBeenCalled;
  }

  public boolean hasPostRemoveBeenCalled() {
    return hasPostRemoveBeenCalled;
  }
}

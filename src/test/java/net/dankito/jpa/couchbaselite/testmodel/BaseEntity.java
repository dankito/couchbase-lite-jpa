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
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * Created by ganymed on 15/08/16.
 */
@MappedSuperclass
public class BaseEntity {

  @Id
  protected String id;

  @Version
  protected Long version = 0L;

  @Column(name = "created_on", updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date createdOn;

  @Column(name = "modified_on")
  @Temporal(TemporalType.TIMESTAMP)
  protected Date modifiedOn;


  @Transient // test both, Transient annotation and key word
  protected boolean hasPrePersistBeenCalled = false;
  protected transient boolean hasPostPersistBeenCalled = false;

  protected transient boolean hasPostLoadBeenCalled = false;

  @Transient
  protected boolean hasPreUpdateBeenCalled = false;
  protected transient boolean hasPostUpdateBeenCalled = false;

  @Transient
  protected boolean hasPreRemoveBeenCalled = false;
  protected transient boolean hasPostRemoveBeenCalled = false;



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

  public void setId(String id) {
    this.id = id;
  }

  public Long getVersion() {
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

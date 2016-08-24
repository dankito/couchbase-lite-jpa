package net.dankito.jpa.couchbaselite.testmodel.inheritance;

import net.dankito.jpa.couchbaselite.testmodel.BaseEntity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * Created by ganymed on 24/08/16.
 */
@Entity
@Table(name = "Joined")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public class JoinedTableBase extends BaseEntity {

  public static final String NAME_COLUMN_NAME = "name";


  @Column(name = NAME_COLUMN_NAME)
  protected String name;


  public JoinedTableBase() {

  }

  public JoinedTableBase(String name) {
    this.name = name;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}

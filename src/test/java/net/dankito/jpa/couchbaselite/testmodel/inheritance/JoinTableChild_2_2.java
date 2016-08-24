package net.dankito.jpa.couchbaselite.testmodel.inheritance;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by ganymed on 24/08/16.
 */
@Entity
@DiscriminatorValue(value = "Child_2_2")
public class JoinTableChild_2_2 extends JoinTableChild_2_MappedSuperclass {

  public static final String SALARY_COLUMN_NAME = "salary";


  @Column(name = SALARY_COLUMN_NAME)
  protected BigDecimal salary;


  public JoinTableChild_2_2() {

  }

  public JoinTableChild_2_2(String name, String givenName, BigDecimal salary) {
    super(name, givenName);
    this.salary = salary;
  }


  public BigDecimal getSalary() {
    return salary;
  }

  public void setSalary(BigDecimal salary) {
    this.salary = salary;
  }

}

package net.dankito.jpa.util;


public enum CrudOperation {

  CREATE("create"),
  RETRIEVE("retrieve"),
  UPDATE("update"),
  DELETE("delete");


  private String englishName;


  CrudOperation(String englishName) {
    this.englishName = englishName;
  }


  @Override
  public String toString() {
    return englishName;
  }

}

package net.dankito.jpa.util;

import net.dankito.jpa.apt.config.ColumnConfig;


public interface IValueConverter {

  Object convertRetrievedValue(ColumnConfig property, Object retrievedValue);

  Object convertValueForPersistence(ColumnConfig property, Object propertyValue);

}

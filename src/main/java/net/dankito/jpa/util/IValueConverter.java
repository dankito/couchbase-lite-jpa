package net.dankito.jpa.util;

import net.dankito.jpa.annotationreader.config.PropertyConfig;

/**
 * Created by ganymed on 25/08/16.
 */
public interface IValueConverter {
  Object convertRetrievedValue(PropertyConfig property, Object retrievedValue);

  Object convertValueForPersistence(PropertyConfig property, Object propertyValue);
}

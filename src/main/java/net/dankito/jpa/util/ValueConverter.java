package net.dankito.jpa.util;

import net.dankito.jpa.annotationreader.config.PropertyConfig;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by ganymed on 25/08/16.
 */
public class ValueConverter {

  public Object convertRetrievedValue(PropertyConfig property, Object retrievedValue) {
    Object convertedValue = retrievedValue;

    if(property.getType() == Date.class && retrievedValue instanceof Long) {
      convertedValue = convertToDate(retrievedValue);
    }
    else if(property.getType().isEnum() && retrievedValue instanceof Enum == false) {
      convertedValue = convertToEnum(property, retrievedValue);
    }
    else if(property.getType() == BigDecimal.class && retrievedValue instanceof BigDecimal == false) {
      convertedValue = convertToBigDecimal(retrievedValue);
    }

    return convertedValue;
  }

  protected Object convertToDate(Object retrievedValue) {
    Object convertedValue = retrievedValue;

    // TODO: add parsing Date and Time formats
    if(retrievedValue instanceof Long) {
      convertedValue = new Date((long)retrievedValue);
    }

    return convertedValue;
  }

  protected Object convertToEnum(PropertyConfig property, Object retrievedValue) {
    Object convertedValue = retrievedValue;

    if(retrievedValue instanceof String) {
      convertedValue = Enum.valueOf(property.getType(), (String)retrievedValue);
    }
    else if(retrievedValue instanceof Integer) {
      int ordinal = (int)retrievedValue;
      for(Object enumValue : property.getType().getEnumConstants()) {
        if(((Enum)enumValue).ordinal() == ordinal) {
          convertedValue = enumValue;
        }
      }
    }

    return convertedValue;
  }

  protected Object convertToBigDecimal(Object retrievedValue) {
    Object convertedValue = retrievedValue;

    if(retrievedValue instanceof Integer) {
      convertedValue = new BigDecimal((Integer) retrievedValue);
    }
    else if(retrievedValue instanceof Long) {
      convertedValue = new BigDecimal((Long) retrievedValue);
    }
    else if(retrievedValue instanceof Double) {
      convertedValue = new BigDecimal((Double) retrievedValue);
    }
    else if(retrievedValue instanceof String) {
      convertedValue = new BigDecimal((String) retrievedValue);
    }

    return convertedValue;
  }

}

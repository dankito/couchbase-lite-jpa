package net.dankito.jpa.util;

import net.dankito.jpa.apt.config.ColumnConfig;
import net.dankito.jpa.apt.config.DataType;

import java.math.BigDecimal;
import java.util.Date;


public class ValueConverter implements IValueConverter {

  @Override
  public Object convertRetrievedValue(ColumnConfig property, Object retrievedValue) {
    if(retrievedValue == null) {
      return retrievedValue;
    }

    Object convertedValue = retrievedValue;

    if(property.getClassType() == Date.class) {
      if(retrievedValue instanceof Long || retrievedValue instanceof Integer)
      convertedValue = convertToDate(retrievedValue);
    }
    else if(property.isEnumType() && retrievedValue instanceof Enum == false) {
      convertedValue = convertToEnum(property, retrievedValue);
    }
    else if(property.getClassType() == BigDecimal.class && retrievedValue instanceof BigDecimal == false) {
      convertedValue = convertToBigDecimal(retrievedValue);
    }
    else if((property.getClassType() == float.class || property.getClassType() == Float.class)
        && retrievedValue instanceof Float == false) {
      if (retrievedValue instanceof Double) {
        convertedValue = ((Double) retrievedValue).floatValue();
      }
    }

    return convertedValue;
  }

  protected Object convertToDate(Object retrievedValue) {
    Object convertedValue = retrievedValue;

    // TODO: add parsing Date and Time formats
    if(retrievedValue instanceof Long) {
      convertedValue = new Date((long)retrievedValue);
    }
    else if(retrievedValue instanceof Integer) {
      convertedValue = new Date((int)retrievedValue);
    }

    return convertedValue;
  }

  protected Object convertToEnum(ColumnConfig property, Object retrievedValue) {
    Object convertedValue = retrievedValue;

    if(retrievedValue instanceof String) {
      convertedValue = Enum.valueOf(property.getClassType().asSubclass(Enum.class), (String)retrievedValue);
    }
    else if(retrievedValue instanceof Integer) {
      int ordinal = (int)retrievedValue;
      for(Object enumValue : property.getClassType().getEnumConstants()) {
        if(((Enum)enumValue).ordinal() == ordinal) {
          convertedValue = enumValue;
          break;
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

  @Override
  public Object convertValueForPersistence(ColumnConfig property, Object propertyValue) {
    if(propertyValue == null) {
      return propertyValue;
    }

    Object persistableValue = propertyValue;

    if(Date.class.equals(property.getType())) {
      // TODO: find a better way to set @Temporal
      if(DataType.DATE_TIMESTAMP.equals(property.getDataType())) {
        if(propertyValue instanceof Date) {
          persistableValue = ((Date) propertyValue).getTime();
        }
      }
      // TODO: implement others
    }
    else if(property.isEnumType()) {
      if(property.getDataType() == DataType.ENUM_STRING) {
        persistableValue = propertyValue.toString();
      }
      else if(property.getDataType() == DataType.ENUM_INTEGER) {
        persistableValue = ((Enum)propertyValue).ordinal();
      }
    }

    return persistableValue;
  }
}

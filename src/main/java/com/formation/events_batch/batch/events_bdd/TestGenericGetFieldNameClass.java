package com.formation.events_batch.batch.events_bdd;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;

public class TestGenericGetFieldNameClass<T> {

  public String[] getFields(T t) {
    try {
      PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(t.getClass(), Object.class)
          .getPropertyDescriptors();
      return Arrays.stream(propertyDescriptors).map(PropertyDescriptor::getName).toArray(String[]::new);

    } catch (IntrospectionException e) {
      return new String[] {};
    }
  }
}

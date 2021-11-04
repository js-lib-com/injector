package com.jslib.injector;

import java.lang.annotation.Annotation;

import javax.inject.Named;

import js.util.Strings;

/**
 * Utility class for named qualifier annotations.
 * 
 * @author Iulian Rotaru
 */
public class Names
{

  /**
   * Get {@link Named} annotation wrapping given string value. Returned annotation is guaranteed to have hash code and
   * equals predicate based on string value parameter.
   * 
   * @param value string value for {@literal @Named} annotation.
   * @return {@literal @Named} annotation.
   */
  public static Named named(final String value)
  {
    return new Named()
    {
      @Override
      public Class<? extends Annotation> annotationType()
      {
        return Named.class;
      }

      @Override
      public String value()
      {
        return value;
      }

      @Override
      public int hashCode()
      {
        // see java.lang.Annotation#hashCode()
        return (127 * "value".hashCode()) ^ value.hashCode();
      }

      @Override
      public boolean equals(Object o)
      {
        if(!(o instanceof Named)) {
          return false;
        }
        Named other = (Named)o;
        return value.equals(other.value());
      }

      @Override
      public String toString()
      {
        return Strings.concat("@", Named.class.getName(), "(value=", value, ")");
      }
    };
  }

}

package com.jslib.injector;

import java.lang.annotation.Annotation;
import java.util.Objects;

import js.util.Strings;

/**
 * Instance class key is a qualified type used to uniquely identify the instance class to retrieve. It is a compound key
 * with mandatory instance type and an optional qualifier; if missing, qualifier is null. Key type is immutable but
 * qualifier can be changed after instance key creation.
 * 
 * When used with a provisioning provider all generated instances are assignable to key type; anyway, all instances are
 * different. When used with a scoped provider this key identifies cached instance so that the same instance is
 * retrieved in a given scope. For that purpose there is {@link #toScope()}.
 * 
 * WARN: This key implementation assume that supplied qualifier annotations properly implement hash code and equals
 * accordingly their attributes.
 * 
 * @author Iulian Rotaru
 */
public class Key<T>
{

  /**
   * Create an instance key with null qualifier.
   * 
   * @param type instance type.
   * @param <T> type parameter.
   * @return key instance.
   */
  public static <T> Key<T> get(Class<T> type)
  {
    return new Key<>(type);
  }

  /**
   * Create a qualified instance key. Qualifier annotation should implement hash code and equals accordingly its
   * attributes.
   * 
   * @param type instance type,
   * @param qualifier instance qualifier annotation.
   * @param <T> type parameter.
   * @return key instance.
   */
  public static <T> Key<T> get(Class<T> type, Annotation qualifier)
  {
    return new Key<>(type, qualifier);
  }

  /**
   * Create a qualified instance key. Qualifier annotation should implement hash code and equals accordingly its
   * attributes.
   * 
   * @param type instance type.
   * @param qualifier instance qualifier annotation type.
   * @param <T> type parameter.
   * @return key instance.
   */
  public static <T> Key<T> get(Class<T> type, Class<? extends Annotation> qualifier)
  {
    return new Key<>(type, qualifier);
  }

  // --------------------------------------------------------------------------------------------

  private final Class<T> type;
  private Object qualifier;

  private Key(Class<T> type)
  {
    this.type = type;
  }

  private Key(Class<T> type, Annotation qualifier)
  {
    this.type = type;
    this.qualifier = qualifier;
  }

  private Key(Class<T> type, Class<? extends Annotation> qualifier)
  {
    this.type = type;
    this.qualifier = qualifier;
  }

  public Class<T> type()
  {
    return type;
  }

  public void setQualifier(Annotation qualifier)
  {
    this.qualifier = qualifier;
  }

  public void setQualifier(Class<? extends Annotation> qualifier)
  {
    this.qualifier = qualifier;
  }

  /**
   * Key value for scoped providers used to uniquely identify a cached instance for reuse. This value is a string
   * serving the same purpose as {@link #hashCode()}.
   * 
   * @return scoped key.
   */
  public String toScope()
  {
    // I do not found any guaranty that hashCode cannot clash ever and is critical to have uniqueness
    // otherwise instances can be mixed in critical scopes like HTTP sessions

    // using class (canonical) name ensure consistency across class loaders

    if(qualifier == null) {
      return type.getCanonicalName();
    }

    StringBuilder builder = new StringBuilder();
    builder.append(type.getCanonicalName());
    if(qualifier instanceof Class) {
      builder.append(((Class<?>)qualifier).getCanonicalName());
    }
    else {
      // critical assumption:
      // this logic require that qualifier annotation properly implement hash code accordingly its attributes
      builder.append(Integer.toString(qualifier.hashCode()));
    }

    return Strings.concat(type.getCanonicalName(), qualifier);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(qualifier, type);
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    Key<?> other = (Key<?>)obj;
    return Objects.equals(qualifier, other.qualifier) && Objects.equals(type, other.type);
  }

  @Override
  public String toString()
  {
    return Strings.toString(type, qualifier);
  }

}

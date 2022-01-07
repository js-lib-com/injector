package com.jslib.injector;

import java.lang.annotation.Annotation;

/**
 * Meta interface for <code>@Scope</code> annotations, both {@link jakarta.enterprise.context.NormalScope normal scope}
 * and {@link jakarta.inject.Scope pseudo-scopes}. This utility interface supports both Jakarta and Javax packages.
 * 
 * @author Iulian Rotaru
 */
public interface IScope
{

  static boolean isPresent(Annotation annotation)
  {
    return isPresent(annotation.annotationType());
  }

  static boolean isPresent(Class<? extends Annotation> annotation)
  {
    if(annotation.isAnnotationPresent(jakarta.enterprise.context.NormalScope.class)) {
      return true;
    }
    if(annotation.isAnnotationPresent(javax.enterprise.context.NormalScope.class)) {
      return true;
    }
    if(annotation.isAnnotationPresent(jakarta.inject.Scope.class)) {
      return true;
    }
    if(annotation.isAnnotationPresent(javax.inject.Scope.class)) {
      return true;
    }
    return false;
  }

}

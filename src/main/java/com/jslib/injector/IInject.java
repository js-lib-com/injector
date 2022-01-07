package com.jslib.injector;

import java.lang.reflect.AnnotatedElement;

/**
 * Meta interface for <code>@Inject</code> annotation, both Jakarta and Javax packages.
 * 
 * @author Iulian Rotaru
 */
public interface IInject
{

  static boolean isPresent(AnnotatedElement element)
  {
    if(element.isAnnotationPresent(jakarta.inject.Inject.class)) {
      return true;
    }
    if(element.isAnnotationPresent(javax.inject.Inject.class)) {
      return true;
    }
    return false;
  }

}

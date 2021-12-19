package com.jslib.injector;

import java.lang.annotation.Annotation;

public interface IQualifier
{

  static boolean isPresent(Annotation annotation)
  {
    return isPresent(annotation.annotationType());
  }

  static boolean isPresent(Class<? extends Annotation> annotation)
  {
    return annotation.isAnnotationPresent(jakarta.inject.Qualifier.class) || annotation.isAnnotationPresent(javax.inject.Qualifier.class);
  }


}

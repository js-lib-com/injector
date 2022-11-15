package com.jslib.injector;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.jslib.api.injector.IInjector;
import com.jslib.api.injector.IProvisionInvocation;
import com.jslib.api.injector.ITypedProvider;
import com.jslib.api.injector.Key;
import com.jslib.api.injector.ProvisionException;
import com.jslib.api.log.Log;
import com.jslib.api.log.LogFactory;
import com.jslib.util.Params;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Qualifier;

/**
 * Provisioning provider creates new instances every time it is invoked; it has no scope cache.
 * 
 * @author Iulian Rotaru
 */
class ProvisioningProvider<T> implements ITypedProvider<T>
{
  private static final Log log = LogFactory.getLog(ProvisioningProvider.class);

  private final IInjector injector;
  private final Class<? extends T> type;

  private final Constructor<? extends T> constructor;
  private final List<ParameterKey<?>> constructorParameters;
  private final List<FieldKey<?>> fields;
  private final List<MethodKey> methods;

  public ProvisioningProvider(IInjector injector, Class<? extends T> type)
  {
    Params.notNull(injector, "Injector");
    Params.isInstantiable(type, "Type");

    this.injector = injector;
    this.type = type;

    this.constructor = constructor(type);
    this.constructorParameters = parameterKeys(this.constructor);
    this.fields = fields(type);
    this.methods = methods(type);
  }

  @Override
  public Class<? extends T> type()
  {
    return type;
  }

  @Override
  public T get()
  {
    try (CircularDependencyGuard guard = new CircularDependencyGuard()) {
      // guard pop is performed by AutoCloseable#close()
      guard.push(type);

      List<Object> arguments = new ArrayList<>();
      for(ParameterKey<?> parameter : constructorParameters) {
        arguments.add(parameter.value());
      }
      T instance = constructor.newInstance(arguments.toArray());

      for(FieldKey<?> field : fields) {
        field.set(instance);
      }
      for(MethodKey method : methods) {
        method.invoke(instance);
      }

      log.trace("Create instance {java_type}.", type);
      injector.fireEvent(IProvisionInvocation.create(this, instance));
      return instance;
    }
    catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      log.dump(String.format("Provider fails to create instance %s:", type.getCanonicalName()), e);
      throw new ProvisionException(e);
    }
  }

  @Override
  public String toString()
  {
    return type.getCanonicalName() + ":CLASS";
  }

  static <T> Constructor<T> constructor(Class<T> type)
  {
    @SuppressWarnings("unchecked")
    Constructor<T>[] declaredConstructors = (Constructor<T>[])type.getDeclaredConstructors();
    assert declaredConstructors.length > 0;
    Constructor<T> defaultConstructor = null;
    Constructor<T> constructor = null;

    for(Constructor<T> declaredConstructor : declaredConstructors) {
      // synthetic constructors are created by compiler to circumvent JVM limitations, JVM that is not evolving with
      // the same speed as the language; for example, to allow outer class to access private members on a nested class
      // compiler creates a constructor with a single argument of very nested class type
      if(declaredConstructor.isSynthetic()) {
        continue;
      }

      if(declaredConstructor.isAnnotationPresent(Inject.class)) {
        if(constructor != null) {
          throw new ProvisionException("Invalid implementation class |%s|. Multiple constructors marked with @Inject.", type);
        }
        constructor = declaredConstructor;
        continue;
      }

      if(declaredConstructor.getParameterCount() == 0) {
        defaultConstructor = declaredConstructor;
        continue;
      }
    }

    if(constructor == null) {
      if(defaultConstructor == null) {
        throw new ProvisionException("Invalid implementation class |%s|. Missing default constructor or constructor marked with @Inject.", type);
      }
      constructor = defaultConstructor;
    }
    constructor.setAccessible(true);
    return constructor;
  }

  private List<FieldKey<?>> fields(Class<?> type)
  {
    List<FieldKey<?>> fields = new ArrayList<>();
    for(Field field : type.getDeclaredFields()) {
      if(field.isAnnotationPresent(Inject.class)) {
        field.setAccessible(true);
        fields.add(new FieldKey<>(field, Key.get(field.getType(), qualifier(field))));
      }
    }
    return fields;
  }

  private List<MethodKey> methods(Class<?> type)
  {
    List<MethodKey> methods = new ArrayList<>();
    for(Method method : type.getDeclaredMethods()) {
      if(method.isAnnotationPresent(Inject.class)) {
        List<ParameterKey<?>> parameterKeys = parameterKeys(method);
        Key<?>[] keys = new Key<?>[parameterKeys.size()];
        for(int i = 0; i < keys.length; ++i) {
          keys[i] = parameterKeys.get(i).key;
        }

        method.setAccessible(true);
        methods.add(new MethodKey(method, keys));
      }
    }
    return methods;
  }

  /**
   * Get instance keys for constructor or method parameters that can be qualified. Returned list is in declaration
   * order. Returned keys may return qualifier annotation if present on parameter.
   * 
   * @param executable executable element: constructor or method.
   * @return executable parameter keys.
   */
  private List<ParameterKey<?>> parameterKeys(Executable executable)
  {
    List<ParameterKey<?>> keys = new ArrayList<>();
    for(Parameter parameter : executable.getParameters()) {
      keys.add(new ParameterKey<>(parameter, Key.get(parameter.getType(), qualifier(parameter))));
    }
    return keys;
  }

  /**
   * Return qualifier annotation of the requested annotated element or null if not present. A qualifier is an annotation
   * that has {@link Qualifier} annotation.
   * 
   * @param element annotated element: method or field.
   * @return qualifier annotation or null if not present.
   */
  private static Annotation qualifier(AnnotatedElement element)
  {
    for(Annotation annotation : element.getAnnotations()) {
      if(annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
        return annotation;
      }
    }
    return null;
  }

  private static Class<?> providerTypeArgument(Type type)
  {
    if(!(type instanceof ParameterizedType)) {
      throw new ProvisionException("Missing parameter argument from provider.");
    }
    return (Class<?>)((ParameterizedType)type).getActualTypeArguments()[0];
  }

  private class ParameterKey<P>
  {
    final Parameter parameter;
    final Key<P> key;

    public ParameterKey(Parameter parameter, Key<P> key)
    {
      this.parameter = parameter;
      this.key = key;
    }

    Object value()
    {
      if(!parameter.getType().equals(Provider.class)) {
        return injector.getInstance(key);
      }
      return new ProxyProvider<>(injector, key.forType(providerTypeArgument(parameter.getParameterizedType())));
    }
  }

  private class FieldKey<F>
  {
    final Field field;
    final Key<F> key;

    public FieldKey(Field field, Key<F> key)
    {
      this.field = field;
      this.key = key;
    }

    void set(Object instance) throws IllegalArgumentException, IllegalAccessException
    {
      try {
        Object value = null;
        if(field.getType().equals(Provider.class)) {
          value = new ProxyProvider<>(injector, key.forType(providerTypeArgument(field.getGenericType())));
        }
        else {
          value = injector.getInstance(key);
        }

        field.set(instance, value);
      }
      catch(RuntimeException e) {
        throw new ProvisionException("Fail to inject |%s| to field |%s:%s|. Root cause: %s: %s", key, field.getDeclaringClass().getCanonicalName(), field.getName(), e.getClass().getCanonicalName(), e.getMessage());
      }
    }
  }

  private class MethodKey
  {
    final Method method;
    final Key<?>[] keys;

    public MethodKey(Method method, Key<?>[] keys)
    {
      this.method = method;
      this.keys = keys;
    }

    void invoke(Object instance) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
      try {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] arguments = new Object[keys.length];
        for(int i = 0; i < arguments.length; ++i) {
          if(parameterTypes[i].equals(Provider.class)) {
            arguments[i] = new ProxyProvider<>(injector, keys[i].forType(providerTypeArgument(method.getGenericParameterTypes()[i])));
          }
          else {
            arguments[i] = injector.getInstance(keys[i]);
          }
        }

        method.invoke(instance, arguments);
      }
      catch(RuntimeException e) {
        throw new ProvisionException("Fail to inject |%s| to method |%s:%s|. Root cause: %s: %s", keys, method.getDeclaringClass().getCanonicalName(), method.getName(), e.getClass().getCanonicalName(), e.getMessage());
      }
    }
  }
}

package com.jslib.injector;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Qualifier;

import js.injector.IInjector;
import js.injector.IProvisionInvocation;
import js.injector.ITypedProvider;
import js.injector.Key;
import js.injector.ProvisionException;
import js.log.Log;
import js.log.LogFactory;
import js.util.Params;

/**
 * Provisioning provider creates new instances every time it is invoked; it has no scope cache.
 * 
 * @author Iulian Rotaru
 */
class ProvisioningProvider<T> implements ITypedProvider<T>
{
  private static final Log log = LogFactory.getLog(ProvisioningProvider.class);

  /** Thread local storage for dependencies trace stack. Used to prevent circular dependencies. */
  private static ThreadLocal<Stack<Class<?>>> dependenciesStack = new ThreadLocal<>();

  private final IInjector injector;
  private final Class<? extends T> type;

  private final Constructor<? extends T> constructor;
  private final List<Key<?>> constructorKeys;
  private final List<FieldKey<?>> fields;
  private final List<MethodKey<?>> methods;

  public ProvisioningProvider(IInjector injector, Class<? extends T> type)
  {
    Params.notNull(injector, "Injector");
    Params.isInstantiable(type, "Type");

    this.injector = injector;
    this.type = type;

    this.constructor = getConstructor(type);
    this.constructorKeys = getParameterKeys(this.constructor);
    this.fields = getFields(type);
    this.methods = getMethods(type);
  }

  @Override
  public Class<? extends T> type()
  {
    return type;
  }

  @Override
  public T get()
  {
    Stack<Class<?>> stackTrace = dependenciesStack.get();
    if(stackTrace == null) {
      stackTrace = new Stack<>();
      dependenciesStack.set(stackTrace);
    }

    if(stackTrace.contains(type)) {
      try {
        // add current dependency class to reveal what dependency from stack is circular
        stackTrace.add(type);

        StringBuilder builder = new StringBuilder();
        builder.append("Circular dependency. Dependencies trace follows:\r\n");
        for(Class<?> stackTraceClass : stackTrace) {
          builder.append("\t- ");
          builder.append(stackTraceClass.getName());
          builder.append("\r\n");
        }
        log.error(builder.toString());

        throw new IllegalStateException(String.format("Circular dependency on |%s|. See stack trace on logger.", type.getName()));
      }
      finally {
        // takes care to current thread stack trace is removed
        dependenciesStack.remove();
      }
    }

    stackTrace.push(type);
    try {
      List<Object> arguments = new ArrayList<>();
      for(Key<?> key : constructorKeys) {
        arguments.add(injector.getInstance(key));
      }
      T instance = constructor.newInstance(arguments.toArray());

      for(FieldKey<?> field : fields) {
        field.set(instance);
      }
      for(MethodKey<?> method : methods) {
        method.invoke(instance);
      }

      log.debug("Create instance for |%s|.", type);
      injector.fireEvent(IProvisionInvocation.create(this, instance));
      return instance;
    }
    catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      log.error(e);
      throw new ProvisionException(e);
    }
    finally {
      stackTrace.pop();
      // do not remove stack trace after outermost call finished, i.e. when stack trace is empty
      // leave it on thread local for reuse, in order to avoid unnecessary object creation
    }
  }

  @Override
  public String toString()
  {
    return type.getCanonicalName() + ":CLASS";
  }

  static <T> Constructor<T> getConstructor(Class<T> type)
  {
    @SuppressWarnings("unchecked")
    Constructor<T>[] declaredConstructors = (Constructor<T>[])type.getDeclaredConstructors();
    if(declaredConstructors.length == 0) {
      throw new ProvisionException("Invalid implementation class |%s|. Missing constructor.", type);
    }
    Constructor<T> defaultConstructor = null;
    Constructor<T> constructor = null;

    for(Constructor<T> declaredConstructor : declaredConstructors) {
      // synthetic constructors are created by compiler to circumvent JVM limitations, JVM that is not evolving with
      // the same speed as the language; for example, to allow outer class to access private members on a nested class
      // compiler creates a constructor with a single argument of very nested class type
      if(declaredConstructor.isSynthetic()) {
        continue;
      }

      if(isInjected(declaredConstructor)) {
        constructor = declaredConstructor;
        break;
      }

      if(declaredConstructor.getParameters().length == 0) {
        defaultConstructor = declaredConstructor;
        continue;
      }
      constructor = declaredConstructor;
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

  private List<FieldKey<?>> getFields(Class<?> type)
  {
    List<FieldKey<?>> fields = new ArrayList<>();
    for(Field field : type.getDeclaredFields()) {
      if(isInjected(field)) {
        field.setAccessible(true);
        fields.add(new FieldKey<>(field, Key.get(field.getType(), getQualifier(field))));
      }
    }
    return fields;
  }

  private List<MethodKey<?>> getMethods(Class<?> type)
  {
    List<MethodKey<?>> methods = new ArrayList<>();
    for(Method method : type.getDeclaredMethods()) {
      if(isInjected(method)) {
        List<Key<?>> parameterKeys = getParameterKeys(method);
        if(parameterKeys.size() != 1) {
          throw new IllegalArgumentException("Invalid inject method " + method);
        }
        method.setAccessible(true);
        methods.add(new MethodKey<>(method, parameterKeys.get(0)));
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
  private static List<Key<?>> getParameterKeys(Executable executable)
  {
    List<Key<?>> keys = new ArrayList<>();
    for(Parameter parameter : executable.getParameters()) {
      keys.add(Key.get(parameter.getType(), getQualifier(parameter)));
    }
    return keys;
  }

  /**
   * Test if annotated element has {@link Inject} annotation.
   * 
   * @param element annotated element: constructor, method or field.
   * @return true if annotated element has {@link Inject} annotation.
   */
  private static boolean isInjected(AnnotatedElement element)
  {
    return element.getAnnotation(Inject.class) != null;
  }

  /**
   * Return qualifier annotation of the requested annotated element or null if not present. A qualifier is an annotation
   * that has {@link Qualifier} annotation.
   * 
   * @param element annotated element: method or field.
   * @return qualifier annotation or null if not present.
   */
  private static Annotation getQualifier(AnnotatedElement element)
  {
    for(Annotation annotation : element.getAnnotations()) {
      if(annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
        return annotation;
      }
    }
    return null;
  }

  private class FieldKey<F>
  {
    Field field;
    Key<F> key;

    public FieldKey(Field field, Key<F> key)
    {
      this.field = field;
      this.key = key;
    }

    void set(Object instance) throws IllegalArgumentException, IllegalAccessException
    {
      field.set(instance, injector.getInstance(key));
    }
  }

  private class MethodKey<M>
  {
    Method method;
    Key<M> key;

    public MethodKey(Method method, Key<M> key)
    {
      this.method = method;
      this.key = key;
    }

    void invoke(Object instance) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
      method.invoke(instance, injector.getInstance(key));
    }
  }
}

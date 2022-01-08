package com.jslib.injector;

import static java.lang.String.format;
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
import java.util.Stack;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Qualifier;
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
  private final List<ParameterKey<?>> constructorParameters;
  private final List<FieldKey<?>> fields;
  private final List<MethodKey<?>> methods;

  public ProvisioningProvider(IInjector injector, Class<? extends T> type)
  {
    Params.notNull(injector, "Injector");
    Params.isInstantiable(type, "Type");

    this.injector = injector;
    this.type = type;

    this.constructor = getConstructor(type);
    this.constructorParameters = getParameterKeys(this.constructor);
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
        builder.append(format("Circular dependency on |%s|. Dependencies trace follows:\r\n", type.getName()));
        for(Class<?> stackTraceClass : stackTrace) {
          builder.append("\t- ");
          builder.append(stackTraceClass.getName());
          builder.append("\r\n");
        }
        log.error(builder.toString());

        throw new IllegalStateException(format("Circular dependency on |%s|. See stack trace on logger.", type.getName()));
      }
      finally {
        // takes care to current thread stack trace is removed
        dependenciesStack.remove();
      }
    }

    stackTrace.push(type);
    try {
      List<Object> arguments = new ArrayList<>();
      for(ParameterKey<?> parameter : constructorParameters) {
        arguments.add(parameter.value());
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
        List<ParameterKey<?>> parameterKeys = getParameterKeys(method);
        if(parameterKeys.size() != 1) {
          throw new IllegalArgumentException("Invalid inject method " + method);
        }
        method.setAccessible(true);
        methods.add(new MethodKey<>(method, parameterKeys.get(0).key));
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
  private List<ParameterKey<?>> getParameterKeys(Executable executable)
  {
    List<ParameterKey<?>> keys = new ArrayList<>();
    for(Parameter parameter : executable.getParameters()) {
      keys.add(new ParameterKey<>(parameter, Key.get(parameter.getType(), getQualifier(parameter))));
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
    return IInject.isPresent(element);
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
      if(IQualifier.isPresent(annotation)) {
        return annotation;
      }
    }
    return null;
  }

  private static Class<?> getTypeArgument(Type type)
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
      return new ProxyProvider<>(injector, key.forType(getTypeArgument(parameter.getParameterizedType())));
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
          value = new ProxyProvider<>(injector, key.forType(getTypeArgument(field.getGenericType())));
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

  private class MethodKey<M>
  {
    final Method method;
    final Key<M> key;

    public MethodKey(Method method, Key<M> key)
    {
      this.method = method;
      this.key = key;
    }

    void invoke(Object instance) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
      try {
        Object value = null;
        // method is already validated; it surely has one parameter
        if(method.getParameterTypes()[0].equals(Provider.class)) {
          value = new ProxyProvider<>(injector, key.forType(getTypeArgument(method.getGenericParameterTypes()[0])));
        }
        else {
          value = injector.getInstance(key);
        }

        method.invoke(instance, value);
      }
      catch(RuntimeException e) {
        throw new ProvisionException("Fail to inject |%s| to method |%s:%s|. Root cause: %s: %s", key, method.getDeclaringClass().getCanonicalName(), method.getName(), e.getClass().getCanonicalName(), e.getMessage());
      }
    }
  }
}

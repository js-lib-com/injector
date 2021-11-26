package com.jslib.injector;

import java.lang.annotation.Annotation;

import javax.inject.Named;

/**
 * Injector facade deals with bindings configuration, instances creation and provisioning events.
 * 
 * @author Iulian Rotaru
 */
public interface IInjector
{

  void configure(IModule... modules);

  <T> IBindingBuilder<T> getBindingBuilder(Class<T> type);

  /**
   * Gets instance identified by given instance key. Depending on declared bindings, returned instance can be newly
   * created or reused from some scope cache. Anyway, a binding for requested instance key must exist; otherwise
   * unchecked provision exception is thrown.
   * 
   * @param key instance key composed from type and optional qualifier.
   * @return instance, newly created or reused from scope cache.
   * @param <T> generic instance type.
   * @throws ProvisionException if there is no bindings for requested instance key.
   */
  <T> T getInstance(Key<T> key);

  /**
   * Convenient alternative of {@link #getInstance(Key)} when instance qualifier is missing.
   * 
   * @param type instance type.
   * @return instance, newly created or reused from scope cache.
   * @param <T> generic instance type.
   * @throws ProvisionException if there is no bindings for requested instance type.
   */
  <T> T getInstance(Class<T> type);

  /**
   * Convenient alternative of {@link #getInstance(Key)} when instance qualifier is {@link Named}.
   * 
   * @param type instance type,
   * @param name instance name as declared by {@link Named} annotation value.
   * @return instance, newly created or reused from scope cache.
   * @param <T> generic instance type.
   * @throws ProvisionException if there is no bindings for requested instance type and name.
   */
  <T> T getInstance(Class<T> type, String name);

  /**
   * Get cached instance from a scope provider or null on cache miss. This method should not trigger cache update on
   * target scoped provider. It is merely intended to detect if cache has an instance for requested type. Return also
   * null if there are no bindings for requested type.
   * 
   * @param type instance type.
   * @return cached instance or null on cache miss.
   * @param <T> generic instance type.
   */
  <T> T getScopeInstance(Class<T> type);

  void bindListener(IProvisionListener provisionListener);

  void unbindListener(IProvisionListener provisionListener);

  <T> void fireEvent(IProvisionInvocation<T> provisionInvocation);

  <T> void bindScope(Class<? extends Annotation> annotation, IScope<T> scope);

  <T> IScope<T> getScope(Class<? extends Annotation> annotation);

}

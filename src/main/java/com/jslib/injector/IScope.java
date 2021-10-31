package com.jslib.injector;

import javax.inject.Provider;

import com.jslib.injector.impl.SingletonScopeProvider;

/**
 * A scope has a cache and controls instances life span. It is applied as a decorator on provisioning provider; remember
 * that a provisioning provider always creates a new instance.
 * 
 * @author Iulian Rotaru
 */
public interface IScope
{

  /**
   * Create a scope provider by decorating given provisioning provider.
   * 
   * @param key instance key,
   * @param provisioningProvider provisioning provider, delegated for actual instances creation.
   * @return scope provider.
   * @param <T> generic instance type.
   */
  <T> Provider<T> scope(Key<T> key, Provider<T> provisioningProvider);

  /**
   * Clear caches. Although public, this method is not intended for clients business code. It is designed for testing
   * code, providing means to decouple test cases that otherwise would mix up via static caches.
   * 
   * @hidden
   */
  static void clearCache()
  {
    SingletonScopeProvider.clearCache();
  }

}

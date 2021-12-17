package com.jslib.injector;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;
import javax.inject.Singleton;

import js.injector.IBinding;
import js.injector.IInjector;
import js.injector.IScopeFactory;
import js.injector.Key;
import js.injector.ScopedProvider;
import js.log.Log;
import js.log.LogFactory;

class SingletonScopeProvider<T> extends ScopedProvider<T>
{
  private static final Log log = LogFactory.getLog(SingletonScopeProvider.class);

  private static final Map<Key<?>, Object> cache = new HashMap<>();

  public static void clearCache()
  {
    log.debug("Clear cache.");
    cache.clear();
  }

  private final Key<T> key;

  /**
   * Construct this singleton provider instance. Because is not allowed to nest the scoped providers, throws illegal
   * argument if given provisioning provider argument is a scoped provider instance.
   * 
   * @param key instance key for which this singleton provider is created.
   * @param provisioningProvider wrapped provisioning provider.
   * @throws IllegalArgumentException if provisioning provider argument is a scoped provider instance.
   */
  private SingletonScopeProvider(Key<T> key, Provider<T> provisioningProvider)
  {
    super(provisioningProvider);
    this.key = key;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getScopeInstance()
  {
    return (T)cache.get(key);
  }

  @Override
  public Class<? extends Annotation> getScope()
  {
    return Singleton.class;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T get()
  {
    Object instance = getScopeInstance();
    if(instance == null) {
      synchronized(this) {
        instance = getScopeInstance();
        if(instance == null) {
          instance = getProvisioningProvider().get();
          cache.put(key, instance);
        }
      }
    }
    return (T)instance;
  }

  @Override
  public String toString()
  {
    return getProvisioningProvider().toString() + ":SINGLETON";
  }

  // --------------------------------------------------------------------------------------------

  public static class Factory<T> implements IScopeFactory<T>
  {
    @Override
    public Provider<T> getScopedProvider(IInjector injector, IBinding<T> provisioningBinding)
    {
      return new SingletonScopeProvider<>(provisioningBinding.key(), provisioningBinding.provider());
    }
  }
}
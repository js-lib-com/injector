package com.jslib.injector;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import js.injector.IScope;
import js.injector.Key;
import js.injector.ScopedProvider;
import js.log.Log;
import js.log.LogFactory;

class ThreadScopeProvider<T> extends ScopedProvider<T>
{
  private static final Log log = LogFactory.getLog(ThreadScopeProvider.class);

  private static final Map<Key<?>, ThreadLocal<?>> pool = new HashMap<>();

  public static void clearCache()
  {
    log.debug("Clear cache.");
    pool.clear();
  }

  private final Key<T> key;

  /**
   * Construct this thread provider instance. Because is not allowed to nest the scoped providers, throws illegal
   * argument if given provisioning provider argument is a scoped provider instance.
   * 
   * @param key instance key for which this thread provider is created.
   * @param provisioningProvider wrapped provisioning provider.
   * @throws IllegalArgumentException if provisioning provider argument is a scoped provider instance.
   */
  private ThreadScopeProvider(Key<T> key, Provider<T> provisioningProvider)
  {
    super(provisioningProvider);
    this.key = key;
  }

  @Override
  public T getScopeInstance()
  {
    return tls().get();
  }

  @Override
  public T get()
  {
    final ThreadLocal<T> tls = tls();
    T instance = tls.get();
    if(instance == null) {
      instance = getProvisioningProvider().get();
      tls.set(instance);
    }
    return instance;
  }

  @SuppressWarnings("unchecked")
  private ThreadLocal<T> tls()
  {
    ThreadLocal<?> tls = pool.get(key);
    if(tls == null) {
      synchronized(pool) {
        tls = pool.get(key);
        if(tls == null) {
          tls = new ThreadLocal<>();
          pool.put(key, tls);
        }
      }
    }
    return (ThreadLocal<T>)tls;
  }

  @Override
  public String toString()
  {
    return getProvisioningProvider().toString() + ":THREAD";
  }

  // --------------------------------------------------------------------------------------------

  public static class Factory<T> implements IScope<T>
  {
    @Override
    public Provider<T> scope(Key<T> key, Provider<T> provider)
    {
      return new ThreadScopeProvider<>(key, provider);
    }
  }
}
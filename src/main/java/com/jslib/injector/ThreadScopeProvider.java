package com.jslib.injector;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Provider;
import js.injector.IBinding;
import js.injector.IInjector;
import js.injector.IScopeFactory;
import js.injector.Key;
import js.injector.ScopedProvider;
import js.injector.ThreadScoped;
import js.log.Log;
import js.log.LogFactory;

/**
 * Provider for {@link ThreadScoped} instances, bound to current thread. Thread scoped instance is created on the fly
 * and reused from cache as long the parent thread is alive.
 * 
 * Note that thread scoped instances are bound to current thread only but not inherited by child threads. For this
 * reason this provider implementation uses {@link ThreadLocal} but not the {@link InheritableThreadLocal}.
 * 
 * @author Iulian Rotaru
 */
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
  public Class<? extends Annotation> getScope()
  {
    return ThreadScoped.class;
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
          // do not use InheritableThreadLocal since thread scoped instance should be bound only to current thread
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

  public static class Factory<T> implements IScopeFactory<T>
  {
    @Override
    public Provider<T> getScopedProvider(IInjector injector, IBinding<T> provisioningBinding)
    {
      return new ThreadScopeProvider<>(provisioningBinding.key(), provisioningBinding.provider());
    }
  }
}
package com.jslib.injector.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import com.jslib.injector.IScope;
import com.jslib.injector.Key;
import com.jslib.injector.ScopedProvider;

public class ThreadScopeProvider<T> extends ScopedProvider<T>
{
  private static final Map<Key<?>, ThreadLocal<?>> pool = new HashMap<>();

  public static void clearCache()
  {
    pool.clear();
  }

  private final Key<T> key;

  private ThreadScopeProvider(Key<T> key, Provider<T> provider)
  {
    super(provider);
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
      instance = provider.get();
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
    return provider.toString() + ":THREAD";
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
package com.jslib.injector;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import js.injector.IScope;
import js.injector.Key;
import js.injector.ScopedProvider;

class SingletonScopeProvider<T> extends ScopedProvider<T>
{
  private static final Map<Key<?>, Object> cache = new HashMap<>();

  public static void clearCache()
  {
    cache.clear();
  }

  private final Key<T> key;

  private SingletonScopeProvider(Key<T> key, Provider<T> provider)
  {
    super(provider);
    this.key = key;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getScopeInstance()
  {
    return (T)cache.get(key);
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
          instance = provider.get();
          cache.put(key, instance);
        }
      }
    }
    return (T)instance;
  }

  @Override
  public String toString()
  {
    return provider.toString() + ":SINGLETON";
  }

  // --------------------------------------------------------------------------------------------

  public static class Factory<T> implements IScope<T>
  {
    @Override
    public Provider<T> scope(Key<T> key, Provider<T> provider)
    {
      return new SingletonScopeProvider<>(key, provider);
    }
  }
}
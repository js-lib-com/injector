package com.jslib.injector.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import com.jslib.injector.IScope;
import com.jslib.injector.Key;
import com.jslib.injector.ScopedProvider;

class SingletonScopeProvider<T> extends ScopedProvider<T>
{
  private static final Map<Key<?>, Object> cache = new HashMap<>();

  private final Key<T> key;

  SingletonScopeProvider(Key<T> key, Provider<T> provider)
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

  public static class Factory implements IScope
  {
    @Override
    public <T> Provider<T> scope(Key<T> key, Provider<T> provider)
    {
      return new SingletonScopeProvider<>(key, provider);
    }
  }
}
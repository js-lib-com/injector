package com.jslib.injector;

import java.util.HashMap;
import java.util.Map;

import com.jslib.api.injector.Key;

class SingletonCache
{
  private final Map<Key<?>, Object> cache = new HashMap<>();

  @SuppressWarnings("unchecked")
  public <T> T get(Key<T> key)
  {
    return (T)cache.get(key);
  }

  public void put(Key<?> key, Object instance)
  {
    cache.put(key, instance);
  }
}

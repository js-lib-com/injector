package com.jslib.injector;

import jakarta.inject.Provider;
import js.injector.IInjector;
import js.injector.Key;

public class ProxyProvider<T> implements Provider<T>
{
  private final IInjector injector;
  private final Key<T> key;

  public ProxyProvider(IInjector injector, Key<T> key)
  {
    this.injector = injector;
    this.key = key;
  }

  @Override
  public T get()
  {
    return injector.getInstance(key);
  }
}

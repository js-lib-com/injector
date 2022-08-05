package com.jslib.injector;

import com.jslib.api.injector.IBinding;
import com.jslib.api.injector.ITypedProvider;
import com.jslib.api.injector.Key;

import jakarta.inject.Provider;

class Binding<T> implements IBinding<T>
{
  private final Key<T> key;
  private Provider<T> provider;

  public Binding(Class<T> type)
  {
    this.key = Key.get(type);
  }

  public Binding(Class<T> type, ITypedProvider<T> provider)
  {
    this.key = Key.get(type);
    this.provider = provider;
  }

  @Override
  public Key<T> key()
  {
    return key;
  }

  @Override
  public Provider<T> provider()
  {
    return provider;
  }

  public void setProvider(Provider<T> provider)
  {
    this.provider = provider;
  }
}
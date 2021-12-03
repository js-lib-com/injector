package com.jslib.injector;

import js.injector.ITypedProvider;

class InstanceProvider<T> implements ITypedProvider<T>
{
  private final T instance;

  public InstanceProvider(T instance)
  {
    this.instance = instance;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends T> type()
  {
    return (Class<? extends T>)instance.getClass();
  }

  @Override
  public T get()
  {
    return instance;
  }

  @Override
  public String toString()
  {
    return instance.getClass().getCanonicalName() + ":INSTANCE";
  }
}

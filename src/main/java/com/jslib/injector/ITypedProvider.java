package com.jslib.injector;

import javax.inject.Provider;

public interface ITypedProvider<T> extends Provider<T>
{
  default Class<? extends T> type()
  {
    return null;
  };
}

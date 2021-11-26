package com.jslib.injector;

import javax.inject.Provider;

public abstract class ScopedProvider<T> implements Provider<T>
{
  protected Provider<T> provider;

  protected ScopedProvider(Provider<T> provider)
  {
    this.provider = provider;
  }

  public void setProvider(Provider<T> provider)
  {
    this.provider = provider;
  }

  public Provider<T> getProvisioningProvider()
  {
    return provider;
  }

  public abstract T getScopeInstance();
}
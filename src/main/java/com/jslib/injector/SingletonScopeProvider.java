package com.jslib.injector;

import java.lang.annotation.Annotation;

import com.jslib.api.injector.IBinding;
import com.jslib.api.injector.IInjector;
import com.jslib.api.injector.IScopeFactory;
import com.jslib.api.injector.Key;
import com.jslib.api.injector.ScopedProvider;

import jakarta.inject.Singleton;

class SingletonScopeProvider<T> extends ScopedProvider<T>
{
  private final SingletonCache cache;
  private final Key<T> key;

  /**
   * Construct this singleton provider instance. Because is not allowed to nest the scoped providers, throws illegal
   * argument if given provisioning provider argument is a scoped provider instance.
   * 
   * @param injector parent injector,
   * @param provisioningBinding binding for provisioning provider.
   * @throws IllegalArgumentException if provisioning provider argument is a scoped provider instance.
   */
  private SingletonScopeProvider(IInjector injector, IBinding<T> provisioningBinding)
  {
    super(provisioningBinding.provider());
    this.cache = ((Injector)injector).getSingletonCache();
    this.key = provisioningBinding.key();
  }

  @Override
  public T getScopeInstance()
  {
    return cache.get(key);
  }

  @Override
  public Class<? extends Annotation> getScope()
  {
    return Singleton.class;
  }

  @Override
  public T get()
  {
    T instance = getScopeInstance();
    if(instance == null) {
      synchronized(this) {
        instance = getScopeInstance();
        if(instance == null) {
          instance = getProvisioningProvider().get();
          cache.put(key, instance);
        }
      }
    }
    return instance;
  }

  @Override
  public String toString()
  {
    return getProvisioningProvider().toString() + ":SINGLETON";
  }

  // --------------------------------------------------------------------------------------------

  public static class Factory<T> implements IScopeFactory<T>
  {
    @Override
    public SingletonScopeProvider<T> getScopedProvider(IInjector injector, IBinding<T> provisioningBinding)
    {
      return new SingletonScopeProvider<>(injector, provisioningBinding);
    }
  }
}
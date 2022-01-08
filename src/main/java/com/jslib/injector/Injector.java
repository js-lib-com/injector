package com.jslib.injector;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Provider;
import js.injector.IBindingBuilder;
import js.injector.IInjector;
import js.injector.IModule;
import js.injector.IProvisionInvocation;
import js.injector.IProvisionListener;
import js.injector.IScopeFactory;
import js.injector.Key;
import js.injector.Names;
import js.injector.ProvisionException;
import js.log.Log;
import js.log.LogFactory;

public class Injector implements IInjector
{
  private static final Log log = LogFactory.getLog(Injector.class);

  private final Map<Class<? extends Annotation>, IScopeFactory<?>> scopeFactories = new HashMap<>();

  private final SingletonCache singletonCache = new SingletonCache();

  private final Map<Key<?>, Provider<?>> bindings = new HashMap<>();

  private final Set<IProvisionListener> provisionListeners = Collections.synchronizedSet(new HashSet<>());

  public Injector()
  {
    log.trace("Injector()");

    bindScopeFactory(jakarta.inject.Singleton.class, new SingletonScopeProvider.Factory<>());
    bindScopeFactory(jakarta.enterprise.context.ApplicationScoped.class, new SingletonScopeProvider.Factory<>());

    bindScopeFactory(javax.inject.Singleton.class, new SingletonScopeProvider.Factory<>());
    bindScopeFactory(javax.enterprise.context.ApplicationScoped.class, new SingletonScopeProvider.Factory<>());
  }

  @Override
  public IInjector configure(IModule... modules)
  {
    log.trace("configure(Module...)");
    if(!bindings.isEmpty()) {
      throw new IllegalStateException("Injector instance already configured.");
    }

    // make this injector instance available via its interface
    bindings.put(Key.get(IInjector.class), new InstanceProvider<>(this));

    for(IModule module : modules) {
      module.configure(this).bindings().forEach(binding -> {
        log.debug("Bind |%s| to provider |%s|.", binding.key(), binding.provider());
        bindings.put(binding.key(), binding.provider());
      });
    }
    return this;
  }

  @Override
  public <T> IBindingBuilder<T> getBindingBuilder(Class<T> type)
  {
    Binding<T> binding = type.isInterface() ? new Binding<>(type) : new Binding<>(type, new ProvisioningProvider<>(this, type));
    return new BindingBuilder<>(this, binding);
  }

  @Override
  public <T> IBindingBuilder<T> getBindingBuilder(Class<T> type, T instance)
  {
    Binding<T> binding = new Binding<>(type, new InstanceProvider<>(instance));
    return new BindingBuilder<>(this, binding);
  }

  @Override
  public <T> T getInstance(Class<T> type)
  {
    return getInstance(Key.get(type));
  }

  @Override
  public <T> T getInstance(Key<T> key)
  {
    @SuppressWarnings("unchecked")
    Provider<T> provider = (Provider<T>)bindings.get(key);
    if(provider == null) {
      throw new ProvisionException("No injector binding for " + key);
    }
    return provider.get();
  }

  @Override
  public <T> T getInstance(Class<T> type, String name)
  {
    return getInstance(Key.get(type, Names.named(name)));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Provider<T> getProvider(Class<T> type)
  {
    return (Provider<T>)bindings.get(Key.get(type));
  }

  @Override
  public void bindListener(IProvisionListener provisionListener)
  {
    provisionListeners.add(provisionListener);
  }

  @Override
  public void unbindListener(IProvisionListener provisionListener)
  {
    provisionListeners.remove(provisionListener);
  }

  @Override
  public <T> void fireEvent(IProvisionInvocation<T> provisionInvocation)
  {
    provisionListeners.forEach(listener -> listener.onProvision(provisionInvocation));
  }

  @Override
  public <T> void bindScopeFactory(Class<? extends Annotation> annotation, IScopeFactory<T> scope)
  {
    if(!IScope.isPresent(annotation)) {
      throw new IllegalArgumentException("Not a scope annotation: " + annotation);
    }
    log.debug("Register |%s| to scope |%s|.", annotation, scope);
    scopeFactories.put(annotation, scope);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> IScopeFactory<T> getScopeFactory(Class<? extends Annotation> annotation)
  {
    return (IScopeFactory<T>)scopeFactories.get(annotation);
  }

  public SingletonCache getSingletonCache()
  {
    return singletonCache;
  }
}

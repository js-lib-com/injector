package com.jslib.injector;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;
import javax.inject.Scope;
import javax.inject.Singleton;

import js.injector.IBindingBuilder;
import js.injector.IInjector;
import js.injector.IModule;
import js.injector.IProvisionInvocation;
import js.injector.IProvisionListener;
import js.injector.IScope;
import js.injector.Key;
import js.injector.Names;
import js.injector.ProvisionException;
import js.injector.ScopedProvider;
import js.injector.ThreadScoped;
import js.log.Log;
import js.log.LogFactory;

public class Injector implements IInjector
{
  private static final Log log = LogFactory.getLog(Injector.class);

  private final Map<Class<? extends Annotation>, IScope<?>> scopes = new HashMap<>();

  private final Map<Key<?>, Provider<?>> bindings = new HashMap<>();

  private final Set<IProvisionListener> provisionListeners = Collections.synchronizedSet(new HashSet<>());

  public Injector()
  {
    // clear scope caches, just in case injector is recreated inside the same JVM, e.g. unit tests
    clearCache();

    bindScope(Singleton.class, new SingletonScopeProvider.Factory<>());
    bindScope(ThreadScoped.class, new ThreadScopeProvider.Factory<>());
  }

  @Override
  public IInjector configure(IModule... modules)
  {
    log.trace("configure(Module...)");
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
    Binding<T> binding = type.isInterface() ? new Binding<>(type) : new Binding<>(type, new ClassProvider<>(this, type));
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
      throw new ProvisionException("No provider for " + key);
    }
    return provider.get();
  }

  @Override
  public <T> T getInstance(Class<T> type, String name)
  {
    return getInstance(Key.get(type, Names.named(name)));
  }

  @Override
  public <T> T getScopeInstance(Class<T> type)
  {
    Key<T> key = Key.get(type);
    @SuppressWarnings("unchecked")
    Provider<T> provider = (Provider<T>)bindings.get(key);
    if(provider == null) {
      log.debug("No provider for |%s|.", key);
      return null;
    }
    if(!(provider instanceof ScopedProvider)) {
      log.debug("Not a scoped provider |%s|.", provider);
      return null;
    }
    ScopedProvider<T> scopedProvider = (ScopedProvider<T>)provider;
    return scopedProvider.getScopeInstance();
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
  public <T> void bindScope(Class<? extends Annotation> annotation, IScope<T> scope)
  {
    if(!annotation.isAnnotationPresent(Scope.class)) {
      throw new IllegalArgumentException("Not a scope annotation: " + annotation);
    }
    log.debug("Register |%s| to scope |%s|.", annotation, scope);
    scopes.put(annotation, scope);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> IScope<T> getScope(Class<? extends Annotation> annotation)
  {
    return (IScope<T>)scopes.get(annotation);
  }

  @Override
  public void clearCache()
  {
    SingletonScopeProvider.clearCache();
    ThreadScopeProvider.clearCache();
  }
}

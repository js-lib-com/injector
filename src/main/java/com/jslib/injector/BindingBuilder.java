package com.jslib.injector;

import java.lang.annotation.Annotation;
import java.net.URI;

import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Scope;

import js.injector.IBinding;
import js.injector.IBindingBuilder;
import js.injector.IInjector;
import js.injector.IScope;
import js.injector.ITypedProvider;

class BindingBuilder<T> implements IBindingBuilder<T>
{
  private final IInjector injector;
  private final Binding<T> binding;

  public BindingBuilder(IInjector injector, Binding<T> binding)
  {
    this.injector = injector;
    this.binding = binding;
  }

  @Override
  public IBindingBuilder<T> with(Annotation qualifier)
  {
    if(!qualifier.annotationType().isAnnotationPresent(Qualifier.class)) {
      throw new IllegalArgumentException("Not a qualifier annotation: " + qualifier);
    }
    binding.key().setQualifier(qualifier);
    return this;
  }

  @Override
  public IBindingBuilder<T> with(Class<? extends Annotation> qualifierType)
  {
    if(!qualifierType.isAnnotationPresent(Qualifier.class)) {
      throw new IllegalArgumentException("Not a qualifier annotation: " + qualifierType);
    }
    binding.key().setQualifier(qualifierType);
    return this;
  }

  @Override
  public IBindingBuilder<T> to(Class<? extends T> type)
  {
    binding.setProvider(new ClassProvider<>(injector, type));
    return this;
  }

  @Override
  public IBindingBuilder<T> toInstance(T instance)
  {
    return instance(instance);
  }

  @Override
  public IBindingBuilder<T> instance(T instance)
  {
    binding.setProvider(new InstanceProvider<>(instance));
    return this;
  }

  @Override
  public IBindingBuilder<T> in(Class<? extends Annotation> annotation)
  {
    if(!annotation.isAnnotationPresent(Scope.class)) {
      throw new IllegalArgumentException("Not a scope annotation: " + annotation);
    }

    IScope<T> scope = injector.getScope(annotation);
    if(scope == null) {
      throw new IllegalStateException("No scope for annotation " + annotation);
    }

    binding.setProvider(scope.scope(binding.key(), binding.provider()));
    return this;
  }

  @Override
  public IBindingBuilder<T> provider(Provider<T> provider)
  {
    binding.setProvider(provider);
    return this;
  }

  @Override
  public IBindingBuilder<T> provider(ITypedProvider<T> provider)
  {
    binding.setProvider(provider);
    return this;
  }

  @Override
  public IBindingBuilder<T> service()
  {
    binding.setProvider(new ServiceProvider<>(binding.key().type()));
    return this;
  }

  @Override
  public IBindingBuilder<T> on(URI implementationURL)
  {
    binding.setProvider(new RemoteProvider<>(binding.key().type(), implementationURL.toString()));
    return this;
  }

  @Override
  public Provider<T> getProvider()
  {
    return binding.provider();
  }

  @Override
  public IBinding<T> getBinding()
  {
    return binding;
  }
}
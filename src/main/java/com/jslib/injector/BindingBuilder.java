package com.jslib.injector;

import java.lang.annotation.Annotation;
import java.net.URI;

import com.jslib.api.injector.IBinding;
import com.jslib.api.injector.IBindingBuilder;
import com.jslib.api.injector.IInjector;
import com.jslib.api.injector.IScopeFactory;
import com.jslib.api.injector.ITypedProvider;
import com.jslib.api.injector.ScopedProvider;

import jakarta.enterprise.context.NormalScope;
import jakarta.inject.Provider;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;

class BindingBuilder<T> implements IBindingBuilder<T>
{
  private final IInjector injector;
  private final Binding<T> binding;

  public BindingBuilder(IInjector injector, Binding<T> binding)
  {
    this.injector = injector;
    this.binding = binding;

    Provider<T> provider = binding.provider();
    if(provider != null && provider instanceof ITypedProvider) {
      Class<? extends T> implementationClass = ((ITypedProvider<T>)provider).type();
      processScope(implementationClass);
    }
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
  public IBindingBuilder<T> to(Class<? extends T> implementationClass)
  {
    binding.setProvider(new ProvisioningProvider<>(injector, implementationClass));
    processScope(implementationClass);
    return this;
  }

  private boolean processScope(Class<?> implementationClass)
  {
    for(Annotation annotation : implementationClass.getAnnotations()) {
      if(annotation.annotationType().isAnnotationPresent(Scope.class) || annotation.annotationType().isAnnotationPresent(NormalScope.class)) {
        in(annotation.annotationType());
        return true;
      }
    }

    for(Class<?> interfaceClass : implementationClass.getInterfaces()) {
      if(processScope(interfaceClass)) {
        return true;
      }
    }

    return false;
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
    if(!annotation.isAnnotationPresent(Scope.class) && !annotation.isAnnotationPresent(NormalScope.class)) {
      throw new IllegalArgumentException("Not a scope annotation: " + annotation);
    }

    if(binding.provider() instanceof ScopedProvider) {
      binding.setProvider(((ScopedProvider<T>)binding.provider()).getProvisioningProvider());
    }

    IScopeFactory<T> scopeFactory = injector.getScopeFactory(annotation);
    if(scopeFactory == null) {
      throw new IllegalStateException("No scope for annotation " + annotation);
    }

    binding.setProvider(scopeFactory.getScopedProvider(injector, binding));
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
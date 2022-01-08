package com.jslib.injector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Singleton;
import js.injector.IScopeFactory;
import js.injector.ScopedProvider;

@RunWith(MockitoJUnitRunner.class)
public class BindingBuilderTest
{
  @Mock
  private Injector injector;
  @Mock
  private Binding<Service> binding;
  
  @Mock
  private IScopeFactory<Service> scopeFactory;
  @Mock
  private ScopedProvider<Service> sessionScoped;

  private BindingBuilder<Service> builder;

  @Before
  public void beforeTest()
  {
    doReturn(scopeFactory).when(injector).getScopeFactory(SessionScoped.class);
    doReturn(sessionScoped).when(scopeFactory).getScopedProvider(any(), any());
    doReturn(SessionScoped.class).when(sessionScoped).getScope();
    doReturn(new SingletonScopeProvider.Factory<>()).when(injector).getScopeFactory(Singleton.class);

    builder = new BindingBuilder<>(injector, new Binding<>(Service.class));
  }

  @Test
  public void GivenClassWithScopedAnnotation_WhenSetScope_ThenOverrideProvider()
  {
    // given
    builder.to(Service.class);
    assertThat(((ScopedProvider<Service>)builder.getBinding().provider()).getScope(), equalTo(SessionScoped.class));

    // when
    builder.in(Singleton.class);

    // then
    assertThat(builder.getBinding().provider(), instanceOf(ScopedProvider.class));
    assertThat(((ScopedProvider<Service>)builder.getBinding().provider()).getScope(), equalTo(Singleton.class));
  }

  @Test
  public void Given_When_Then()
  {
    // given

    // when

    // then
  }

  @SessionScoped
  private static class Service
  {

  }
}

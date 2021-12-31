package com.jslib.injector;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.inject.Singleton;
import js.injector.IScopeFactory;
import js.injector.ScopedProvider;
import js.injector.ThreadScoped;

@RunWith(MockitoJUnitRunner.class)
public class BindingBuilderTest
{
  @Mock
  private Injector injector;
  @Mock
  private Binding<Service> binding;
  @Mock
  private IScopeFactory<Service> scopeFactory;

  private BindingBuilder<Service> builder;

  @Before
  public void beforeTest()
  {
    doReturn(new SingletonScopeProvider.Factory<>()).when(injector).getScopeFactory(Singleton.class);
    doReturn(new ThreadScopeProvider.Factory<>()).when(injector).getScopeFactory(ThreadScoped.class);

    builder = new BindingBuilder<>(injector, new Binding<>(Service.class));
  }

  @Test
  public void GivenClassWithScopedAnnotation_WhenSetScope_ThenOverrideProvider()
  {
    // given
    builder.to(Service.class);
    assertThat(((ScopedProvider<Service>)builder.getBinding().provider()).getScope(), equalTo(ThreadScoped.class));

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

  @ThreadScoped
  private static class Service
  {

  }
}

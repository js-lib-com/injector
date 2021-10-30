package com.jslib.injector.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jslib.injector.Key;

@RunWith(MockitoJUnitRunner.class)
public class SingletonScopeProviderTest
{
  @Mock
  private Key<Object> instanceKey;
  @Mock
  private Provider<Object> provisioningProvider;

  private SingletonScopeProvider<Object> scopeProvider;

  @Before
  public void beforeTest()
  {
    when(provisioningProvider.get()).thenAnswer(invocation -> new Object());
    
    scopeProvider = new SingletonScopeProvider<>(instanceKey, provisioningProvider);
  }

  @Test
  public void GivenCacheMiss_WhenGetScopeInstance_ThenNull()
  {
    // given

    // when
    Object instance = scopeProvider.getScopeInstance();

    // then
    assertThat(instance, nullValue());
  }

  @Test
  public void GivenCache_WhenGetScopeInstance_ThenNotNull()
  {
    // given
    scopeProvider.get();

    // when
    Object instance = scopeProvider.getScopeInstance();

    // then
    assertThat(instance, notNullValue());
  }

  @Test
  public void GivenCacheMiss_WhenGet_ThenNotNull()
  {
    // given

    // when
    Object instance = scopeProvider.get();

    // then
    assertThat(instance, notNullValue());
  }

  @Test
  public void GivenCache_WhenGet_ThenNotNull()
  {
    // given
    scopeProvider.get();

    // when
    Object instance = scopeProvider.get();

    // then
    assertThat(instance, notNullValue());
  }

  @Test
  public void GivenCachedInstance_WhenGetAnotherInstance_ThenEqual()
  {
    // given
    Object instance1 = scopeProvider.get();

    // when
    Object instance2 = scopeProvider.get();

    // then
    assertThat(instance1, equalTo(instance2));
  }

  @Test
  public void Given_When_Then()
  {
    // given

    // when

    // then
  }
}

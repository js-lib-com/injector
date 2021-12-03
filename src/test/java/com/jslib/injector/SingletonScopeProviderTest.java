package com.jslib.injector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jslib.injector.SingletonScopeProvider.Factory;

import js.injector.Key;

@RunWith(MockitoJUnitRunner.class)
public class SingletonScopeProviderTest
{
  @Mock
  private Key<Object> instanceKey;

  private SingletonScopeProvider<Object> scopeProvider;

  @Before
  public void beforeTest()
  {
    SingletonScopeProvider.Factory<Object> factory=new Factory<>();
    scopeProvider = (SingletonScopeProvider<Object>)factory.scope(instanceKey, () -> new Object());
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
}

package com.jslib.injector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jslib.injector.SingletonScopeProvider.Factory;

import js.injector.IBinding;
import js.injector.Key;

@RunWith(MockitoJUnitRunner.class)
public class SingletonScopeProviderTest
{
  @Mock
  private Injector injector;
  @Mock
  private SingletonCache singletonCache;
  @Mock
  private IBinding<Object> provisioningBinding;
  @Mock
  private Key<Object> instanceKey;

  private SingletonScopeProvider<Object> scopeProvider;

  @Before
  public void beforeTest()
  {
    when(injector.getSingletonCache()).thenReturn(singletonCache);
    
    when(provisioningBinding.key()).thenReturn(instanceKey);
    when(provisioningBinding.provider()).thenReturn(() -> new Object());

    SingletonScopeProvider.Factory<Object> factory = new Factory<>();
    scopeProvider = factory.getScopedProvider(injector, provisioningBinding);
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
    when(singletonCache.get(instanceKey)).thenReturn(new Object());

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
    when(singletonCache.get(instanceKey)).thenReturn(new Object());
    Object instance1 = scopeProvider.get();

    // when
    Object instance2 = scopeProvider.get();

    // then
    assertThat(instance1, equalTo(instance2));
  }
}

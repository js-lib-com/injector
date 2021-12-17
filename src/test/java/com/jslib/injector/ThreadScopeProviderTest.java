package com.jslib.injector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.jslib.injector.ThreadScopeProvider.Factory;

import js.injector.IBinding;
import js.injector.IInjector;
import js.injector.Key;

@RunWith(MockitoJUnitRunner.class)
public class ThreadScopeProviderTest
{
  @Mock
  private IInjector injector;
  @Mock
  private IBinding<Object> provisioningBinding;
  @Mock
  private Key<Object> instanceKey;

  private ThreadScopeProvider<Object> provider;

  @Before
  public void beforeTest()
  {
    when(provisioningBinding.key()).thenReturn(instanceKey);
    when(provisioningBinding.provider()).thenReturn(() -> new Object());

    ThreadScopeProvider.Factory<Object> factory = new Factory<>();
    provider = (ThreadScopeProvider<Object>)factory.getScopedProvider(injector, provisioningBinding);
  }

  @Test
  public void GivenTwoThreads_WhenGet_ThenDifferent() throws InterruptedException
  {
    // given
    class ThreadData
    {
      Object instance;
    }

    // when
    Object instance = provider.get();

    final ThreadData threadData = new ThreadData();
    Thread thread = new Thread(() -> {
      threadData.instance = provider.get();
    });
    thread.start();
    thread.join();

    // then
    assertThat(instance, notNullValue());
    assertThat(threadData.instance, notNullValue());
    assertThat(instance, not(equalTo(threadData.instance)));
  }

  @Test
  public void GivenCacheMiss_WhenGetScopeInstance_ThenNull()
  {
    // given

    // when
    Object instance = provider.getScopeInstance();

    // then
    assertThat(instance, nullValue());
  }

  @Test
  public void GivenCache_WhenGetScopeInstance_ThenNotNull()
  {
    // given
    provider.get();

    // when
    Object instance = provider.getScopeInstance();

    // then
    assertThat(instance, notNullValue());
  }

  @Test
  public void GivenCacheMiss_WhenGet_ThenNotNull()
  {
    // given

    // when
    Object instance = provider.get();

    // then
    assertThat(instance, notNullValue());
  }

  @Test
  public void GivenCache_WhenGet_ThenNotNull()
  {
    // given
    provider.get();

    // when
    Object instance = provider.get();

    // then
    assertThat(instance, notNullValue());
  }

  @Test
  public void GivenCachedInstance_WhenGetAnotherInstance_ThenEqual()
  {
    // given
    Object instance1 = provider.get();

    // when
    Object instance2 = provider.get();

    // then
    assertThat(instance1, equalTo(instance2));
  }
}

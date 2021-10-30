package com.jslib.injector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Singleton;

import org.junit.Before;
import org.junit.Test;

import com.jslib.injector.impl.AbstractModule;
import com.jslib.injector.impl.Injector;

public class SingletonScopeProviderTest
{
  private IInjector injector;

  @Before
  public void beforeTest()
  {
    injector = new Injector();
  }

  @Test
  public void GivenTypeBindAndCacheMiss_WhenGetInstanceByType_ThenNotNull()
  {
    // given
    injector.configure(new AbstractModule()
    {
      @Override
      protected void configure()
      {
        bind(Object.class).in(Singleton.class);
      }
    });

    // when
    Object instance = injector.getInstance(Object.class);

    // then
    assertNotNull(instance);
  }

  @Test
  public void GivenTypeBindAndCache_WhenGetInstanceByType_ThenNotNull()
  {
    // given
    injector.configure(new AbstractModule()
    {
      @Override
      protected void configure()
      {
        bind(Object.class).in(Singleton.class);
      }
    });
    // fill scoped provider cache
    injector.getInstance(Object.class);

    // when
    Object instance = injector.getInstance(Object.class);

    // then
    assertNotNull(instance);
  }

  @Test
  public void GivenTypeBindAndCacheMiss_WhenGetInstanceTwice_ThenEqual()
  {
    // given
    injector.configure(new AbstractModule()
    {
      @Override
      protected void configure()
      {
        bind(Object.class).in(Singleton.class);
      }
    });

    // when
    Object instance1 = injector.getInstance(Object.class);
    Object instance2 = injector.getInstance(Object.class);

    // then
    assertTrue(instance1 == instance2);
  }

  @Test
  public void GivenTypeBindAndCache_WhenGetInstanceTwice_ThenEqual()
  {
    // given
    injector.configure(new AbstractModule()
    {
      @Override
      protected void configure()
      {
        bind(Object.class).in(Singleton.class);
      }
    });
    // fill scoped provider cache
    injector.getInstance(Object.class);

    // when
    Object instance1 = injector.getInstance(Object.class);
    Object instance2 = injector.getInstance(Object.class);

    // then
    assertTrue(instance1 == instance2);
  }

  @Test
  public void GivenTypeBindAndCache_WhenGetInstanceFromThread_ThenEqual() throws InterruptedException
  {
    // given
    injector.configure(new AbstractModule()
    {
      @Override
      protected void configure()
      {
        bind(Object.class).in(Singleton.class);
      }
    });
    // fill scoped provider cache
    injector.getInstance(Object.class);

    // when
    Object instance = injector.getInstance(Object.class);

    class ThreadData
    {
      Object instance;
    }

    ThreadData threadData = new ThreadData();
    Thread thread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        threadData.instance = injector.getInstance(Object.class);
      }
    });
    thread.start();
    thread.join();
    
    // then
    assertTrue(instance == threadData.instance);
  }

  @Test
  public void GivenTwoNamedBindingsAndCacheMiss_WhenGetInstanceByName_ThenNotEqual()
  {
    // given
    injector.configure(new AbstractModule()
    {
      @Override
      protected void configure()
      {
        bind(Object.class).annotatedWith(Names.named("object1")).in(Singleton.class);
        bind(Object.class).annotatedWith(Names.named("object2")).in(Singleton.class);
      }
    });

    // when
    Object instance1 = injector.getInstance(Object.class, "object1");
    Object instance2 = injector.getInstance(Object.class, "object2");

    // then
    assertFalse(instance1 == instance2);
  }

  @Test
  public void GivenTwoNamedBindingsAndCache_WhenGetInstanceByName_ThenNotEqual()
  {
    // given
    injector.configure(new AbstractModule()
    {
      @Override
      protected void configure()
      {
        bind(Object.class).annotatedWith(Names.named("object1")).in(Singleton.class);
        bind(Object.class).annotatedWith(Names.named("object2")).in(Singleton.class);
      }
    });
    // fill scoped provider cache
    injector.getInstance(Object.class, "object1");
    injector.getInstance(Object.class, "object2");

    // when
    Object instance1 = injector.getInstance(Object.class, "object1");
    Object instance2 = injector.getInstance(Object.class, "object2");

    // then
    assertFalse(instance1 == instance2);
  }
}

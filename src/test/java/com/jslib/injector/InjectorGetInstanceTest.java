package com.jslib.injector;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import js.injector.AbstractModule;
import js.injector.IInjector;
import js.injector.Key;
import js.injector.Names;
import js.injector.ProvisionException;

public class InjectorGetInstanceTest
{
  private IInjector injector;

  @Before
  public void beforeTest()
  {
    injector = new Injector();
  }

  @Test
  public void GivenTypeBind_WhenGetInstanceByType_ThenNotNull()
  {
    // given
    injector.configure(new AbstractModule()
    {
      @Override
      protected void configure()
      {
        bind(Object.class);
      }
    });

    // when
    Object instance = injector.getInstance(Object.class);

    // then
    assertNotNull(instance);
  }

  @Test(expected = ProvisionException.class)
  public void GivenNamedQualifiedBind_WhenGetInstanceByType_ThenException()
  {
    // given
    injector.configure(new AbstractModule()
    {
      @Override
      protected void configure()
      {
        bind(Object.class).annotatedWith(Names.named("object"));
      }
    });

    // when
    injector.getInstance(Object.class);

    // then
  }

  @Test
  public void GivenNamedQualifiedBind_WhenGetInstanceByName_ThenNotNull()
  {
    // given
    injector.configure(new AbstractModule()
    {
      @Override
      protected void configure()
      {
        bind(Object.class).annotatedWith(Names.named("object"));
      }
    });

    // when
    Object instance = injector.getInstance(Object.class, "object");

    // then
    assertNotNull(instance);
  }

  @Test
  public void GivenNamedQualifiedBind_WhenGetInstanceByKey_ThenNotNull()
  {
    // given
    injector.configure(new AbstractModule()
    {
      @Override
      protected void configure()
      {
        bind(Object.class).annotatedWith(Names.named("object"));
      }
    });

    // when
    Object instance = injector.getInstance(Key.get(Object.class, Names.named("object")));

    // then
    assertNotNull(instance);
  }
}

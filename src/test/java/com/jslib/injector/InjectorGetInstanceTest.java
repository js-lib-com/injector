package com.jslib.injector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.jslib.api.injector.AbstractModule;
import com.jslib.api.injector.IInjector;
import com.jslib.api.injector.Key;
import com.jslib.api.injector.Names;
import com.jslib.api.injector.ProvisionException;

import jakarta.inject.Inject;

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

  @Test
  public void GivenDefaults_WhenGetInjectorInstance_ThenNotNull()
  {
    // given
    injector.configure();

    // when
    IInjector instance = injector.getInstance(IInjector.class);

    // then
    assertNotNull(instance);
  }

  @Test
  public void GivenInjectorInject_WhenFieldInject_ThenInjectorInstance()
  {
    // given
    injector.configure(new AbstractModule()
    {
      @Override
      protected void configure()
      {
        bind(Service.class);
      }
    });
    
    // when
    Service service = injector.getInstance(Service.class);

    // then
    assertNotNull(service);
    assertNotNull(service.injector);
    assertThat(service.injector, equalTo(injector));
  }

  private static class Service
  {
    @Inject
    IInjector injector;
  }
}

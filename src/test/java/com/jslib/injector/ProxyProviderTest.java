package com.jslib.injector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;
import org.junit.Test;

import com.jslib.api.injector.AbstractModule;
import com.jslib.api.injector.IInjector;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ProxyProviderTest
{
  private IInjector injector;

  @Before
  public void beforeTest()
  {
    injector = new Injector();
    injector.configure(new Module());
  }

  @Test
  public void GivenFieldClient_WhenGetInstance_ThenInjectProvider()
  {
    // given

    // when
    FieldClient client = injector.getInstance(FieldClient.class);

    // then
    assertThat(client, notNullValue());
    assertThat(client.name(), equalTo("Tom Joad"));
  }

  @Test
  public void GivenConstructorClient_WhenGetInstance_ThenInjectProvider()
  {
    // given

    // when
    ConstructorClient client = injector.getInstance(ConstructorClient.class);

    // then
    assertThat(client, notNullValue());
    assertThat(client.name(), equalTo("Tom Joad"));
  }

  @Test
  public void GivenMethodClient_WhenGetInstance_ThenInjectProvider()
  {
    // given

    // when
    MethodClient client = injector.getInstance(MethodClient.class);

    // then
    assertThat(client, notNullValue());
    assertThat(client.name(), equalTo("Tom Joad"));
  }

  // ----------------------------------------------------------------------------------------------

  private static class Service
  {
    public String name()
    {
      return "Tom Joad";
    }
  }

  private static class FieldClient
  {
    @Inject
    private Provider<Service> serviceProvider;

    public String name()
    {
      return serviceProvider.get().name();
    }
  }

  private static class ConstructorClient
  {
    private final Provider<Service> serviceProvider;

    @Inject
    public ConstructorClient(Provider<Service> serviceProvider)
    {
      this.serviceProvider = serviceProvider;
    }

    public String name()
    {
      return serviceProvider.get().name();
    }
  }

  private static class MethodClient
  {
    private Provider<Service> serviceProvider;

    @Inject
    public void setServiceProvider(Provider<Service> serviceProvider)
    {
      this.serviceProvider = serviceProvider;
    }

    public String name()
    {
      return serviceProvider.get().name();
    }
  }

  private static class Module extends AbstractModule
  {
    @Override
    protected void configure()
    {
      bind(FieldClient.class);
      bind(ConstructorClient.class);
      bind(MethodClient.class);
      bind(Service.class);
    }
  }
}

package com.jslib.injector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.lang.reflect.Constructor;

import org.junit.Test;

import com.jslib.api.injector.ProvisionException;

import jakarta.inject.Inject;

@SuppressWarnings("unused")
public class ProvisioningProviderConstructorTest
{
  /** Even class has no constructor defined compiler synthesize the default one. */
  @Test
  public void GivenNoConstructor_WhenGetConstructor_ThenSyhthesizeIt()
  {
    // given

    // when
    Constructor<NoConstructor> constructor = ProvisioningProvider.constructor(NoConstructor.class);

    // then
    assertThat(constructor, notNullValue());
    assertThat(constructor.getParameterCount(), equalTo(0));
  }

  @Test
  public void GivenDefaultConstructor_WhenGetConstructor_ThenGetIt()
  {
    // given

    // when
    Constructor<DefaultConstructor> constructor = ProvisioningProvider.constructor(DefaultConstructor.class);

    // then
    assertThat(constructor, notNullValue());
    assertThat(constructor.getParameterCount(), equalTo(0));
  }

  @Test
  public void GivenNoInject_WhenGetConstructor_ThenDefaultConstructor()
  {
    // given

    // when
    Constructor<NoInject> constructor = ProvisioningProvider.constructor(NoInject.class);

    // then
    assertThat(constructor, notNullValue());
    assertThat(constructor.getParameterCount(), equalTo(0));
  }

  @Test
  public void GivenSingleInjectAndDefault_WhenGetConstructor_ThenInjectConstructor()
  {
    // given

    // when
    Constructor<SingleInjectAndDefault> constructor = ProvisioningProvider.constructor(SingleInjectAndDefault.class);

    // then
    assertThat(constructor, notNullValue());
    assertThat(constructor.getParameterCount(), equalTo(1));
  }

  @Test
  public void GivenSingleInject_WhenGetConstructor_ThenInjectConstructor()
  {
    // given

    // when
    Constructor<SingleInject> constructor = ProvisioningProvider.constructor(SingleInject.class);

    // then
    assertThat(constructor, notNullValue());
    assertThat(constructor.getParameterCount(), equalTo(1));
  }

  @Test
  public void GivenSingleInjectNoDefault_WhenGetConstructor_ThenInjectConstructor()
  {
    // given

    // when
    Constructor<SingleInjectNoDefault> constructor = ProvisioningProvider.constructor(SingleInjectNoDefault.class);

    // then
    assertThat(constructor, notNullValue());
    assertThat(constructor.getParameterCount(), equalTo(1));
  }

  @Test(expected = ProvisionException.class)
  public void GivenNoInjectNoDefault_WhenGetConstructor_ThenException()
  {
    // given

    // when
    ProvisioningProvider.constructor(NoInjectNoDefault.class);

    // then
  }

  @Test(expected = ProvisionException.class)
  public void GivenMultipleInject_WhenGetConstructor_ThenException()
  {
    // given

    // when
    ProvisioningProvider.constructor(MultipleInject.class);

    // then
  }

  // ----------------------------------------------------------------------------------------------
  
  private static class NoConstructor
  {

  }

  private static class DefaultConstructor
  {
    public DefaultConstructor()
    {
    }
  }

  private static class NoInject
  {
    public NoInject()
    {
    }

    public NoInject(String name)
    {
    }
  }

  private static class SingleInjectAndDefault
  {
    public SingleInjectAndDefault()
    {
    }

    @Inject
    public SingleInjectAndDefault(String name)
    {
    }
  }

  private static class SingleInjectNoDefault
  {
    @Inject
    public SingleInjectNoDefault(String name)
    {
    }
  }

  private static class NoInjectNoDefault
  {
    public NoInjectNoDefault(String name)
    {
    }
  }

  private static class SingleInject
  {
    @Inject
    public SingleInject(String name)
    {
    }

    public SingleInject(String name, int age)
    {
    }
  }

  private static class MultipleInject
  {
    public MultipleInject()
    {
    }

    @Inject
    public MultipleInject(String name)
    {
    }

    @Inject
    public MultipleInject(String name, int age)
    {
    }
  }
}

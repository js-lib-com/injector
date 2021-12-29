package com.jslib.injector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.lang.reflect.Constructor;

import javax.inject.Inject;

import org.junit.Test;

public class ProvisioningProviderTest
{
  @SuppressWarnings("unused")
  private ProvisioningProvider<Person> provider;

  @Test
  public void Given_WhenGetConstructor_ThenNotNull()
  {
    // given

    // when
    Constructor<Person> constructor = ProvisioningProvider.getConstructor(Person.class);

    // then
    assertThat(constructor, notNullValue());
    assertThat(constructor.getParameters().length, equalTo(0));
  }

  @Test
  public void Given_When_Then()
  {
    // given

    // when

    // then
  }

  private static class Person
  {
    @SuppressWarnings("unused")
    public Person(String name)
    {
    }

    @SuppressWarnings("unused")
    public Person(String name, int age)
    {
    }

    @Inject
    public Person()
    {
    }
  }
}

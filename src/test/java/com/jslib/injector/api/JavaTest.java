package com.jslib.injector.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.lang.reflect.Constructor;

import org.junit.Test;

public class JavaTest
{
  @Test
  public void GivenNoConstructorClass_WhenGetDeclaredConstructors_ThenSynthesizeIt()
  {
    // given
    Class<?> clazz = NoConstructorClass.class;

    // when
    Constructor<?>[] constructors = clazz.getDeclaredConstructors();

    // then
    assertThat(constructors.length, equalTo(1));
    assertThat(constructors[0].isSynthetic(), equalTo(false));
    assertThat(constructors[0].getParameterCount(), equalTo(0));
  }

  @Test
  public void GivenDefaultConstructorClass_WhenGetDeclaredConstructors_ThenGetIt()
  {
    // given
    Class<?> clazz = DefaultConstructorClass.class;

    // when
    Constructor<?>[] constructors = clazz.getDeclaredConstructors();

    // then
    assertThat(constructors.length, equalTo(1));
    assertThat(constructors[0].isSynthetic(), equalTo(false));
    assertThat(constructors[0].getParameterCount(), equalTo(0));
  }

  @Test
  public void GivenNoDefaultConstructorClass_WhenGetDeclaredConstructors_ThenGetIt()
  {
    // given
    Class<?> clazz = NoDefaultConstructorClass.class;

    // when
    Constructor<?>[] constructors = clazz.getDeclaredConstructors();

    // then
    assertThat(constructors.length, equalTo(1));
    assertThat(constructors[0].isSynthetic(), equalTo(false));
    assertThat(constructors[0].getParameterCount(), equalTo(1));
  }

  private static class NoConstructorClass
  {
  }

  private static class DefaultConstructorClass
  {
    @SuppressWarnings("unused")
    public DefaultConstructorClass()
    {
    }
  }

  private static class NoDefaultConstructorClass
  {
    @SuppressWarnings("unused")
    public NoDefaultConstructorClass(String name)
    {
    }
  }
}

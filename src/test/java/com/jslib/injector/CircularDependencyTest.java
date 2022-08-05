package com.jslib.injector;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.jslib.api.injector.IInjector;
import com.jslib.api.injector.ProvisionException;
import com.jslib.injector.fixture.TestModule;

import jakarta.inject.Inject;

@RunWith(MockitoJUnitRunner.class)
public class CircularDependencyTest
{

  private IInjector injector;

  @Before
  public void beforeTest()
  {
    injector = new Injector();
  }

  @Test
  public void GivenFieldCircularDependency_WhenGetInstance_ThenException()
  {
    // given
    injector.configure(new TestModule(FieldService.class));

    // when
    String exception = null;
    try {
      injector.getInstance(FieldService.class);
    }
    catch(ProvisionException e) {
      exception = e.getMessage();
    }

    // then
    assertThat(exception, notNullValue());
    assertThat(exception, containsString("Circular dependency"));
  }

  @Test
  public void GivenConstructorCircularDependency_WhenGetInstance_ThenException()
  {
    // given
    injector.configure(new TestModule(ConstructorService.class));

    // when
    String exception = null;
    try {
      injector.getInstance(ConstructorService.class);
    }
    catch(IllegalStateException e) {
      exception = e.getMessage();
    }

    // then
    assertThat(exception, notNullValue());
    assertThat(exception, containsString("Circular dependency"));
  }

  @Test
  public void GivenMethodCircularDependency_WhenGetInstance_ThenException()
  {
    // given
    injector.configure(new TestModule(MethodService.class));

    // when
    String exception = null;
    try {
      injector.getInstance(MethodService.class);
    }
    catch(ProvisionException e) {
      exception = e.getMessage();
    }

    // then
    assertThat(exception, notNullValue());
    assertThat(exception, containsString("Circular dependency"));
  }

  @Test
  public void GivenGrandfatherCircularDependency_WhenGetInstance_ThenException()
  {
    // given
    injector.configure(new TestModule(Grandfather.class, Father.class, Son.class));

    // when
    String exception = null;
    try {
      injector.getInstance(Grandfather.class);
    }
    catch(ProvisionException e) {
      exception = e.getMessage();
    }

    // then
    assertThat(exception, notNullValue());
    assertThat(exception, containsString("Circular dependency"));
  }

  // ----------------------------------------------------------------------------------------------

  private static class FieldService
  {
    @SuppressWarnings("unused")
    @Inject
    private FieldService service;
  }

  private static class ConstructorService
  {
    @Inject
    public ConstructorService(ConstructorService service)
    {
    }
  }

  private static class MethodService
  {
    @Inject
    public void setService(MethodService service)
    {
    }
  }

  private static class Grandfather
  {
    @SuppressWarnings("unused")
    @Inject
    private Father father;
  }

  private static class Father
  {
    @SuppressWarnings("unused")
    @Inject
    private Son son;
  }

  private static class Son
  {
    @SuppressWarnings("unused")
    @Inject
    private Grandfather grandfather;
  }
}

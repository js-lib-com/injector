package com.jslib.injector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;
import org.junit.Test;

import com.jslib.api.injector.IInjector;
import com.jslib.injector.fixture.TestModule;

import jakarta.inject.Inject;

public class ConformityTest
{
  private IInjector injector;

  @Before
  public void beforeTest()
  {
    injector = new Injector();
  }

  @Test
  public void GivenTaskService_WhenGetInstance_ThenTaskInjected()
  {
    // given
    injector.configure(new TestModule(Task.class, TaskService.class));

    // when
    TaskService service = injector.getInstance(TaskService.class);

    // then
    assertThat(service.task, notNullValue());
  }

  @Test
  public void GivenTwoFieldsService_WhenGetInstance_ThenBothTasksInjected()
  {
    // given
    injector.configure(new TestModule(Task.class, TwoFieldsService.class));

    // when
    TwoFieldsService service = injector.getInstance(TwoFieldsService.class);

    // then
    assertThat(service.task1, notNullValue());
    assertThat(service.task2, notNullValue());
  }

  @Test
  public void GivenTwoMethodsService_WhenGetInstance_ThenBothTasksInjected()
  {
    // given
    injector.configure(new TestModule(Task.class, TwoMethodsService.class));

    // when
    TwoMethodsService service = injector.getInstance(TwoMethodsService.class);

    // then
    assertThat(service.task1, notNullValue());
    assertThat(service.task2, notNullValue());
  }

  @Test
  public void Given_When_Then()
  {
    // given

    // when

    // then
  }

  // --------------------------------------------------------------------------------------------

  private static class TaskService
  {
    @Inject
    private Task task;
  }

  private static class TwoFieldsService
  {
    @Inject
    private Task task1;

    @Inject
    private Task task2;
  }

  private static class TwoMethodsService
  {
    private Task task1;
    private Task task2;

    @Inject
    public void setTask1(Task task1)
    {
      this.task1 = task1;
    }

    @Inject
    public void setTask2(Task task2)
    {
      this.task2 = task2;
    }
  }

  private static class Task
  {

  }
}

package com.jslib.injector.fixture;

import js.injector.AbstractModule;

public class TestModule extends AbstractModule
{
  private final Class<?>[] types;

  public TestModule(Class<?>... types)
  {
    this.types = types;
  }

  @Override
  protected void configure()
  {
    for(Class<?> type : types) {
      bind(type);
    }
  }
}

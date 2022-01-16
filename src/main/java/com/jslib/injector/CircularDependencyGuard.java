package com.jslib.injector;

import static java.lang.String.format;

import java.util.Stack;

import js.log.Log;
import js.log.LogFactory;

class CircularDependencyGuard implements AutoCloseable
{
  private static final Log log = LogFactory.getLog(CircularDependencyGuard.class);

  /** Thread local storage for dependencies trace stack. Used to prevent circular dependencies. */
  private static ThreadLocal<Stack<Class<?>>> dependenciesStack = new ThreadLocal<>();

  private final Stack<Class<?>> stackTrace;

  public CircularDependencyGuard()
  {
    Stack<Class<?>> stackTrace = dependenciesStack.get();
    if(stackTrace == null) {
      stackTrace = new Stack<>();
      dependenciesStack.set(stackTrace);
    }
    this.stackTrace = stackTrace;
  }

  public void push(Class<?> type)
  {
    if(stackTrace.contains(type)) {
      try {
        // add current dependency class to reveal what dependency from stack is circular
        stackTrace.add(type);

        StringBuilder builder = new StringBuilder();
        builder.append(format("Circular dependency on |%s|. Dependencies trace follows:\r\n", type.getName()));
        for(Class<?> stackTraceClass : stackTrace) {
          builder.append("\t- ");
          builder.append(stackTraceClass.getName());
          builder.append("\r\n");
        }
        log.error(builder.toString());

        throw new IllegalStateException(format("Circular dependency on |%s|. See stack trace on logger.", type.getName()));
      }
      finally {
        // takes care to current thread stack trace is removed
        dependenciesStack.remove();
      }
    }

    stackTrace.push(type);
  }

  @Override
  public void close()
  {
    stackTrace.pop();
    // do not remove stack trace after outermost call finished, i.e. when stack trace is empty
    // leave it on thread local for reuse, in order to avoid unnecessary object creation
  }
}
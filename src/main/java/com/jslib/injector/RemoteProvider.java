package com.jslib.injector;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import com.jslib.api.log.Log;
import com.jslib.api.log.LogFactory;

import js.injector.ITypedProvider;
import js.lang.BugError;
import js.rmi.RemoteFactory;
import js.rmi.RemoteFactoryProvider;
import js.rmi.UnsupportedProtocolException;
import js.util.Params;
import js.util.Strings;

class RemoteProvider<T> implements ITypedProvider<T>
{
  private static final Log log = LogFactory.getLog(RemoteProvider.class);

  private static final Map<String, RemoteFactory> remoteFactories = new HashMap<>();
  static {
    for(RemoteFactoryProvider provider : ServiceLoader.load(RemoteFactoryProvider.class)) {
      for(String protocol : provider.getProtocols()) {
        if(remoteFactories.put(protocol, provider.getRemoteFactory()) != null) {
          throw new BugError("Invalid runtime environment. Remote factory protocol override |%s|. See remote factory provider |%s|.", protocol, provider);
        }
      }
    }
  }

  private final RemoteFactory remoteFactory;
  private final Class<T> type;
  private final String implementationURL;

  public RemoteProvider(Class<T> type, String implementationURL)
  {
    log.trace("RemoteProvider(Class<T>, String)");
    Params.notNullOrEmpty(implementationURL, "Implementation URL");

    String protocol = Strings.getProtocol(implementationURL);
    if(protocol == null) {
      throw new UnsupportedProtocolException("Protocol not found on " + implementationURL);
    }
    this.remoteFactory = remoteFactories.get(protocol);
    if(this.remoteFactory == null) {
      throw new IllegalStateException("No remote factory for protocol " + protocol);
    }

    this.type = type;
    this.implementationURL = implementationURL;
  }

  @Override
  public Class<? extends T> type()
  {
    return type;
  }

  @Override
  public T get()
  {
    return remoteFactory.getRemoteInstance(type, implementationURL);
  }

  @Override
  public String toString()
  {
    return type.getCanonicalName() + ":" + implementationURL;
  }
}
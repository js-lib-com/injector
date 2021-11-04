package com.jslib.injector;

@Deprecated
public interface IBinder {

	<T> IBindingBuilder<T> bind(Class<T> type);
	
}

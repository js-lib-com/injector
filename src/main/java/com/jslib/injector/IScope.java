package com.jslib.injector;

import javax.inject.Provider;

public interface IScope {

	<T> Provider<T> scope(Key<T> key, Provider<T> provider);

}

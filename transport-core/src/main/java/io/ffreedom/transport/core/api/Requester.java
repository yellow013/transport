package io.ffreedom.transport.core.api;

import io.ffreedom.transport.core.TransportModule;

public interface Requester<T> extends TransportModule {

	T request();

}

package io.ffreedom.transport.core.role;

import io.ffreedom.transport.core.TransportModule;

public interface Requester<T> extends TransportModule {

	T request();

}

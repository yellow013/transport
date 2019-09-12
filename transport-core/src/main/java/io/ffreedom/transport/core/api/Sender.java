package io.ffreedom.transport.core.role;

import io.ffreedom.transport.core.TransportModule;

public interface Sender<T> extends TransportModule {

	void send(T msg);

}
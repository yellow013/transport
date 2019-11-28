package io.mercury.transport.core.api;

import io.mercury.transport.core.TransportModule;

public interface Publisher<T> extends TransportModule{

	void publish(T msg);

	void publish(String target, T msg);

}

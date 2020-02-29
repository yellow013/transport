package io.mercury.transport.core.api;

import io.mercury.transport.core.TransportModule;

public interface Publisher<T> extends TransportModule {

	// Publish to default location
	void publish(T msg);

	// Publish to target location
	void publish(String target, T msg);

}

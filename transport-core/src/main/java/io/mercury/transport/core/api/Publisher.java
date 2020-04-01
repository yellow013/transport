package io.mercury.transport.core.api;

import io.mercury.transport.core.TransportModule;
import io.mercury.transport.core.exception.FailPublishException;

public interface Publisher<T> extends TransportModule {

	// Publish to default location
	void publish(T msg) throws FailPublishException;

	// Publish to target location
	void publish(String target, T msg) throws FailPublishException;

}

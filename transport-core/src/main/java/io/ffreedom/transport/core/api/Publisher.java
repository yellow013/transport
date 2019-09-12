package io.ffreedom.transport.core.role;

import io.ffreedom.transport.core.TransportModule;

public interface Publisher<T> extends TransportModule{

	void publish(T msg);

	void publish(String target, T msg);

}

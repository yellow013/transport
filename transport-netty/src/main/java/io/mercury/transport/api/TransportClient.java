package io.mercury.transport.api;

import io.mercury.transport.core.TransportModule;

public interface TransportClient extends TransportModule{

	void connect();

}
